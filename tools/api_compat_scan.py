#!/usr/bin/env python3
"""Checks every Bukkit/Spigot API member the plugin jar references against server jars.

Usage: api_compat_scan.py <plugin.jar> <server.jar> [<server.jar> ...]

Reports, per server jar, references that would fail at run time with NoSuchMethodError / NoSuchFieldError /
NoClassDefFoundError / IncompatibleClassChangeError (a class that used to be an enum or class and is now an interface
or the other way round). Needs no running server. Classes of the plugin and the bundled libraries below
de/happybavarian07 are scanned.
"""
import io
import struct
import sys
import zipfile
from collections import defaultdict

SCAN_PREFIXES = ('de/happybavarian07/',)
API_PREFIXES = ('org/bukkit/', 'net/md_5/bungee/api/', 'org/spigotmc/')
OBJECT_METHODS = {'equals', 'hashCode', 'toString', 'getClass', 'notify', 'notifyAll', 'wait', 'clone', 'finalize'}


class ClassInfo:
    __slots__ = ('name', 'access', 'super', 'interfaces', 'fields', 'methods')


def read_class(data, want_refs=False):
    pos = 8
    count, = struct.unpack_from('>H', data, pos)
    pos += 2
    pool = [None] * count
    i = 1
    while i < count:
        tag = data[pos]
        pos += 1
        if tag == 1:
            length, = struct.unpack_from('>H', data, pos)
            pool[i] = ('utf8', data[pos + 2:pos + 2 + length].decode('utf-8', 'replace'))
            pos += 2 + length
        elif tag in (3, 4):
            pos += 4
        elif tag in (5, 6):
            pos += 8
            i += 1
        elif tag in (7, 8, 16, 19, 20):
            idx, = struct.unpack_from('>H', data, pos)
            pool[i] = ('ref', tag, idx)
            pos += 2
        elif tag in (9, 10, 11, 12, 17, 18):
            a, b = struct.unpack_from('>HH', data, pos)
            pool[i] = ('pair', tag, a, b)
            pos += 4
        elif tag == 15:
            pos += 3
        else:
            raise ValueError('bad constant tag %d' % tag)
        i += 1

    def utf8(index):
        return pool[index][1]

    def class_name(index):
        return utf8(pool[index][2])

    info = ClassInfo()
    info.access, this_idx, super_idx = struct.unpack_from('>HHH', data, pos)
    pos += 6
    info.name = class_name(this_idx)
    info.super = class_name(super_idx) if super_idx else None
    n, = struct.unpack_from('>H', data, pos)
    pos += 2
    info.interfaces = []
    for _ in range(n):
        idx, = struct.unpack_from('>H', data, pos)
        info.interfaces.append(class_name(idx))
        pos += 2
    members = ([], [])
    for kind in (0, 1):
        n, = struct.unpack_from('>H', data, pos)
        pos += 2
        for _ in range(n):
            acc, name_idx, desc_idx, attrs = struct.unpack_from('>HHHH', data, pos)
            pos += 8
            members[kind].append((utf8(name_idx), utf8(desc_idx)))
            for _ in range(attrs):
                length, = struct.unpack_from('>I', data, pos + 2)
                pos += 6 + length
    info.fields = set(members[0])
    info.methods = set(members[1])

    refs = []
    if want_refs:
        for entry in pool:
            if entry and entry[0] == 'pair' and entry[1] in (9, 10, 11):
                owner = class_name(entry[2])
                name_and_type = pool[entry[3]]
                refs.append((entry[1], owner, utf8(name_and_type[2]), utf8(name_and_type[3])))
            elif entry and entry[0] == 'ref' and entry[1] == 7:
                refs.append((7, utf8(entry[2]), None, None))
    return info, refs


def load_api(server_jar):
    classes = {}
    with zipfile.ZipFile(server_jar) as outer:
        names = [n for n in outer.namelist() if n.endswith('.jar') and (
            n.startswith('META-INF/versions/') or 'spigot-api' in n or 'bungeecord' in n)]
        for jar_name in names:
            with zipfile.ZipFile(io.BytesIO(outer.read(jar_name))) as source:
                for name in source.namelist():
                    if name.endswith('.class') and name.startswith(API_PREFIXES):
                        info, _ = read_class(source.read(name))
                        classes[info.name] = info
    return classes


def resolve(classes, owner, name, desc, is_field):
    """True found, False missing, None unknown (chain leaves the API jar)."""
    seen = set()
    unknown = False
    stack = [owner]
    while stack:
        current = stack.pop()
        if current in seen:
            continue
        seen.add(current)
        info = classes.get(current)
        if info is None:
            unknown = True
            continue
        if (name, desc) in (info.fields if is_field else info.methods):
            return True
        if info.super:
            stack.append(info.super)
        stack.extend(info.interfaces)
    if not is_field and name in OBJECT_METHODS:
        return True
    return None if unknown else False


def collect_plugin_refs(plugin_jar):
    refs = defaultdict(set)
    with zipfile.ZipFile(plugin_jar) as jar:
        for name in jar.namelist():
            if not name.endswith('.class') or not name.startswith(SCAN_PREFIXES):
                continue
            try:
                info, found = read_class(jar.read(name), True)
            except (ValueError, struct.error):
                continue
            for ref in found:
                if ref[1].startswith(API_PREFIXES) or ref[1].startswith('[L' + API_PREFIXES[0]):
                    refs[ref].add(info.name)
    return refs


def main():
    plugin_jar, server_jars = sys.argv[1], sys.argv[2:]
    refs = collect_plugin_refs(plugin_jar)
    print('%d distinct API references in %s' % (len(refs), plugin_jar))
    exit_code = 0
    for server_jar in server_jars:
        classes = load_api(server_jar)
        problems = []
        for (tag, owner, name, desc), users in sorted(refs.items(), key=lambda item: str(item[0])):
            target = classes.get(owner)
            if tag == 7:
                if target is None and owner.startswith(API_PREFIXES):
                    problems.append(('missing class', owner, users))
                continue
            if target is None:
                problems.append(('missing class', owner, users))
                continue
            is_interface = bool(target.access & 0x0200)
            if tag == 11 and not is_interface:
                problems.append(('IncompatibleClassChange (now a class)', '%s.%s%s' % (owner, name, desc), users))
                continue
            if tag == 10 and is_interface:
                problems.append(('IncompatibleClassChange (now an interface)', '%s.%s%s' % (owner, name, desc), users))
                continue
            if resolve(classes, owner, name, desc, tag == 9) is False:
                problems.append(('missing field' if tag == 9 else 'missing method', '%s.%s %s' % (owner, name, desc), users))
        print('\n=== %s: %d API classes, %d problems' % (server_jar, len(classes), len(problems)))
        for kind, what, users in problems:
            print('  %s: %s   <- %s' % (kind, what, ', '.join(sorted(u.rsplit('/', 1)[-1] for u in users)[:4])))
        if problems:
            exit_code = 1
    sys.exit(exit_code)


if __name__ == '__main__':
    main()
