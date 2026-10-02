#!/usr/bin/env bash
# Builds AdminPanel once and runs run-adminpanel-smoke.sh on several Spigot versions, each in its own folder below
# "Smoke Matrix". Usage: tools/run-smoke-matrix.sh [version ...]   (default: all versions in the table below)
# AP_FRESH=1 deletes a version's folder first so it starts like a first install.
set -uo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRV="${AP_SERVERS:-F:/Weitere Minecraft Test Server}"
J21="$SRV/jdk-21.0.5/bin/java"
J25="$SRV/jdk-25.0.4.1+1/bin/java"
MATRIX="$SRV/Smoke Matrix"
PLUGINS_FROM="$SRV/Spigot 1.21.10/plugins"

# version | spigot jar | java
TABLE=(
    "1.21.1|$SRV/Spigot 1.21.1/spigot-1.21.1.jar|$J21"
    "1.21.5|$SRV/Spigot 1.21.5/spigot-1.21.5.jar|$J21"
    "1.21.10|$SRV/Spigot 1.21.10/spigot-1.21.10.jar|$J21"
    "1.21.11|$SRV/_builds/spigot-1.21.11.jar|$J21"
    "26.1.2|$SRV/_builds/spigot-26.1.2.jar|$J25"
    "26.2|$SRV/_builds/spigot-26.2.jar|$J25"
    "26.3|$SRV/_builds/spigot-26.3.jar|$J25"
)

mvn -B -q -f "$ROOT/pom.xml" clean package -DskipTests || { echo "Build failed"; exit 1; }

port=25600
results=()
for row in "${TABLE[@]}"; do
    IFS='|' read -r version jar java <<< "$row"
    port=$((port + 1))
    if [ "$#" -gt 0 ] && ! printf '%s\n' "$@" | grep -qx -- "$version"; then continue; fi
    if [ ! -f "$jar" ]; then results+=("$version: no server jar ($jar)"); continue; fi
    dir="$MATRIX/$version"
    [ -n "${AP_FRESH:-}" ] && rm -rf "$dir"
    mkdir -p "$dir/plugins"
    [ -f "$dir/$(basename "$jar")" ] || cp "$jar" "$dir/"
    echo "eula=true" > "$dir/eula.txt"
    [ -f "$dir/server.properties" ] || printf 'online-mode=false\nserver-port=%s\nview-distance=4\nsimulation-distance=4\nspawn-protection=0\nlevel-seed=1\nmax-players=2\nmotd=AdminPanel smoke %s\n' "$port" "$version" > "$dir/server.properties"
    for plugin in PlaceholderAPI-*.jar Vault.jar; do
        [ -f "$dir/plugins/$plugin" ] || cp "$PLUGINS_FROM"/$plugin "$dir/plugins/" 2>/dev/null
    done
    echo "########## $version"
    plugin_jar=$(ls "$ROOT"/target/Admin-Panel-*.jar | grep -v -E -- '-(javadoc|sources|shaded|original)\.jar$' | head -1)
    scan=$(python3 "$ROOT/tools/api_compat_scan.py" "$plugin_jar" "$dir/$(basename "$jar")" 2>&1)
    scan_code=$?
    echo "$scan"
    output=$(AP_SKIP_BUILD=1 AP_SERVER="$dir" AP_JAVA="$java" bash "$ROOT/tools/run-adminpanel-smoke.sh" 2>&1)
    code=$?
    echo "$output"
    summary=$(echo "$output" | grep -m1 -E "Startup: " | sed -E 's/.*\] //')
    results+=("$version: api-scan=$scan_code smoke=$code ${summary:-no startup line}")
done

echo
echo "########## matrix result"
printf '%s\n' "${results[@]}"
