package de.happybavarian07.adminpanel.utils;


import de.happybavarian07.adminpanel.main.AdminPanelMain;

import java.util.logging.Level;

public abstract class VersionComparator {

    public static final VersionComparator EQUALVERSIONS = new VersionComparator() {
        @Override
        public boolean updateAvailable(String pluginVersionString, String spigotVersionString) {
            return !spigotVersionString.equals(pluginVersionString);
        }
    };

    public static final VersionComparator SEMANTIC_VERSION = new VersionComparator() {
        @Override
        public boolean updateAvailable(String pluginVersionString, String spigotVersionString) {
            if (pluginVersionString == null || spigotVersionString == null) return false;
            Version pluginVersion = parseVersion(pluginVersionString);
            Version spigotVersion = parseVersion(spigotVersionString);
            return compare(pluginVersion, spigotVersion) < 0;
        }

        private Version parseVersion(String v) {
            String work = v;
            String build = null;
            int plusIndex = work.indexOf('+');
            if (plusIndex >= 0) {
                build = work.substring(plusIndex + 1);
                work = work.substring(0, plusIndex);
            }
            String pre = null;
            int dashIndex = work.indexOf('-');
            if (dashIndex >= 0) {
                pre = work.substring(dashIndex + 1);
                work = work.substring(0, dashIndex);
            }
            String[] coreParts = work.split("\\.");
            int[] numeric = new int[coreParts.length];
            for (int i = 0; i < coreParts.length; i++) {
                numeric[i] = parseNumericPrefix(coreParts[i]);
            }
            String[] preIdentifiers = pre == null ? null : pre.split("[.-]");
            return new Version(numeric, preIdentifiers, build);
        }

        private int parseNumericPrefix(String part) {
            if (part == null || part.isEmpty()) return 0;
            int j = 0;
            while (j < part.length() && Character.isDigit(part.charAt(j))) j++;
            if (j == 0) return 0;
            try {
                return Integer.parseInt(part.substring(0, j));
            } catch (NumberFormatException e) {
                AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.WARNING, "Failed to parse version part '" + part + "' treating as 0", LogPrefixExtension.UPDATER);
                return 0;
            }
        }

        private int compare(Version a, Version b) {
            int max = Math.max(a.core.length, b.core.length);
            for (int i = 0; i < max; i++) {
                int av = i < a.core.length ? a.core[i] : 0;
                int bv = i < b.core.length ? b.core[i] : 0;
                if (av != bv) return av < bv ? -1 : 1;
            }
            if (a.pre == null && b.pre == null) return 0;
            if (a.pre == null) return 1;
            if (b.pre == null) return -1;
            int maxPre = Math.max(a.pre.length, b.pre.length);
            for (int i = 0; i < maxPre; i++) {
                String ai = i < a.pre.length ? a.pre[i] : null;
                String bi = i < b.pre.length ? b.pre[i] : null;
                if (ai == null && bi == null) return 0;
                if (ai == null) return -1;
                if (bi == null) return 1;
                boolean aNum = isNumeric(ai);
                boolean bNum = isNumeric(bi);
                if (aNum && bNum) {
                    try {
                        long an = Long.parseLong(ai);
                        long bn = Long.parseLong(bi);
                        if (an != bn) return an < bn ? -1 : 1;
                    } catch (NumberFormatException e) {
                        int c = ai.compareTo(bi);
                        if (c != 0) return c < 0 ? -1 : 1;
                    }
                } else if (aNum != bNum) {
                    return aNum ? -1 : 1;
                } else {
                    int c = ai.compareTo(bi);
                    if (c != 0) return c < 0 ? -1 : 1;
                }
            }
            return 0;
        }

        private boolean isNumeric(String s) {
            if (s == null || s.isEmpty()) return false;
            for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
            return true;
        }

        private record Version(int[] core, String[] pre, String build) {
        }
    };

    @Deprecated
    public static final VersionComparator SEMANTIC_VERSIONS = SEMANTIC_VERSION;
    @Deprecated
    public static final VersionComparator SEMATIC_VERSION = SEMANTIC_VERSION;

    public enum VersionStatus {EQUAL, NEWER_AVAILABLE, INVALID_INPUT}

    public static VersionStatus evaluateStatus(String pluginVersionString, String spigotVersionString) {
        if (pluginVersionString == null || spigotVersionString == null || pluginVersionString.isEmpty() || spigotVersionString.isEmpty()) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.WARNING, "Version comparison received null/empty input", LogPrefixExtension.UPDATER);
            return VersionStatus.INVALID_INPUT;
        }
        return SEMANTIC_VERSION.updateAvailable(pluginVersionString, spigotVersionString) ? VersionStatus.NEWER_AVAILABLE : VersionStatus.EQUAL;
    }

    public abstract boolean updateAvailable(String pluginVersionString, String spigotVersionString);
}
