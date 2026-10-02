#!/usr/bin/env bash
# Runs the server-side probe (tools/probe) on the servers of the smoke matrix: spawns every entity type, applies the potion
# presets, changes every gamerule, creates, copies, renames and deletes worlds. Run tools/run-smoke-matrix.sh first so the
# server folders exist, and build the plugin (mvn package -DskipTests).
# Usage: tools/run-probe.sh [version ...]
set -uo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRV="${AP_SERVERS:-F:/Weitere Minecraft Test Server}"
J21="$SRV/jdk-21.0.5"
J25="$SRV/jdk-25.0.4.1+1/bin/java"
MATRIX="$SRV/Smoke Matrix"
API=$(ls ~/.m2/repository/org/spigotmc/spigot-api/1.20.1-R0.1-SNAPSHOT/spigot-api-1.20.1-R0.1-*[0-9].jar | head -1)
PLUGIN=$(ls "$ROOT"/target/Admin-Panel-*.jar | grep -v -- "-shaded" | head -1)
OUT="$ROOT/target/probe"
VERSIONS=(1.21.1 1.21.5 1.21.10 1.21.11 26.1.2 26.2 26.3)
[ "$#" -gt 0 ] && VERSIONS=("$@")

rm -rf "$OUT" && mkdir -p "$OUT/classes"
"$J21/bin/javac" --release 17 -cp "$(cygpath -m "$API");$(cygpath -m "$PLUGIN")" -d "$OUT/classes" "$ROOT/tools/probe/src/ApProbe.java" || exit 1
cp "$ROOT/tools/probe/plugin.yml" "$OUT/classes/"
(cd "$OUT/classes" && "$J21/bin/jar" cf ../ApProbe.jar .)

results=()
for version in "${VERSIONS[@]}"; do
    dir="$MATRIX/$version"
    if [ ! -d "$dir" ]; then results+=("$version: no server folder, run tools/run-smoke-matrix.sh first"); continue; fi
    java="$J21/bin/java"
    case "$version" in 26.*) java="$J25" ;; esac
    jar=$(cd "$dir" && ls spigot-*.jar | head -1)
    cp "$PLUGIN" "$dir/plugins/$(basename "$PLUGIN")"
    cp "$OUT/ApProbe.jar" "$dir/plugins/ApProbe.jar"
    log="$dir/probe.log"
    cmds="$dir/probe-cmds.txt"
    : > "$log"; : > "$cmds"
    (cd "$dir" && tail -f "$cmds" | "$java" -Xms1G -Xmx2G -jar "$jar" nogui > "$log" 2>&1) &
    for _ in $(seq 1 240); do grep -q "PROBE DONE" "$log" 2>/dev/null && break; sleep 1; done
    echo "stop" >> "$cmds"
    for _ in $(seq 1 60); do grep -q "Saving chunks for level .*the_end\|All dimensions are saved" "$log" 2>/dev/null && break; sleep 1; done
    sleep 2
    echo "" >> "$cmds"
    rm -f "$dir/plugins/ApProbe.jar"
    echo "########## $version"
    grep -E "PROBE" "$log" | sed -E 's/^\[[0-9:]+\] \[[^]]*\]: \[ApProbe\] //'
    done_line=$(grep -m1 "PROBE DONE" "$log" | sed -E 's/.*PROBE //')
    results+=("$version: ${done_line:-did not finish}")
done

echo
echo "########## probe result"
printf '%s\n' "${results[@]}"
