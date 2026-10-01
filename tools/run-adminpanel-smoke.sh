#!/usr/bin/env bash
# Builds AdminPanel, starts the 1.21.10 test server, reloads once, stops it and prints AdminPanel errors.
set -uo pipefail
SERVER="${AP_SERVER:-F:/Weitere Minecraft Test Server/Spigot 1.21.10}"
JAVA="${AP_JAVA:-F:/Weitere Minecraft Test Server/jdk-21.0.5/bin/java}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG="$SERVER/adminpanel-smoke.log"
CMDS="$SERVER/adminpanel-smoke-cmds.txt"

mvn -B -q -f "$ROOT/pom.xml" clean package -DskipTests || { echo "Build failed"; exit 1; }
JAR=$(ls "$ROOT"/target/Admin-Panel-*.jar | grep -v -E -- '-(javadoc|sources|shaded)\.jar$' | head -1)
[ -f "$JAR" ] || { echo "No built jar in target/"; exit 1; }
cp "$JAR" "$SERVER/plugins/$(basename "$JAR")"

wait_for() { # pattern, minimum count, timeout seconds
    for _ in $(seq 1 "$3"); do
        [ "$(grep -c -- "$1" "$LOG" 2>/dev/null || true)" -ge "$2" ] && return 0
        sleep 1
    done
    echo "Timed out waiting for '$1' ($2x)"; return 1
}

: > "$CMDS"
: > "$LOG"
(cd "$SERVER" && tail -f "$CMDS" | "$JAVA" -Xms1G -Xmx2G -jar spigot-1.21.10.jar nogui > "$LOG" 2>&1) &

status=0
wait_for "Done (" 1 180 || status=1
for cmd in "openpanel help" "openpanel ServerManagerMenu" "openpanel ServerManagerMenu Nobody" \
           "addon listaddons" "addon info Nothing" "addon disable" \
           "adminpaneladmin reloaddata" "adminpaneladmin reloadconfig" "adminpaneladmin SULControl Enable" \
           "adminpaneladmin backup listBackups" "adminpaneladmin backup start Nothing" "adminpaneladmin permissionmenuhelp" \
           "disableditems list" "disableditems disable NOT_AN_ITEM" "disableditems disable DIAMOND" "disableditems list" "disableditems enable DIAMOND" \
           "warn Nobody Test reason" "warn HappyBavarian07 Smoke reason" "warnings HappyBavarian07" "unwarn HappyBavarian07 all" "warnings HappyBavarian07" \
           "dataclient ListClients" "dataclient SyncTrolls ChatMuteMap" "perplayerlang"; do
    echo "$cmd" >> "$CMDS"
    sleep 1
done
sleep 3
echo "reload confirm" >> "$CMDS"
wait_for "Enabling Admin-Panel" 2 180 || status=1
sleep 8
echo "stop" >> "$CMDS"
wait_for "All dimensions are saved" 1 120 || status=1
sleep 2
echo "" >> "$CMDS" # tail exits on the next write once the server is gone

echo "===== AdminPanel lines ====="
grep -E "\[Admin-Panel\].*(Using Database|Imported old data|Could not|Error|Exception)" "$LOG"
if grep -E "Error occurred while (enabling|disabling) Admin-Panel|Could not load 'plugins.Admin-Panel" "$LOG" | grep -q .; then
    echo "AdminPanel failed to enable or disable"; status=1
fi
if grep -B2 -E "^\s+at de\.happybavarian07\.adminpanel" "$LOG" | grep -q .; then
    echo "AdminPanel stack traces in the log:"
    grep -B3 -m5 -E "^\s+at de\.happybavarian07\.adminpanel" "$LOG"
    status=1
fi
if grep -E "Config Path not found|null path:|An Error occurred!|Error performing sub command" "$LOG" | grep -q .; then
    echo "Missing language keys or failing commands:"
    grep -E "Config Path not found|null path:|An Error occurred!|Error performing sub command" "$LOG" | head -10
    status=1
fi
[ -f "$SERVER/plugin_data.db" ] && { echo "plugin_data.db still in the server root"; status=1; }
exit $status
