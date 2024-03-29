package de.happybavarian07.adminpanel.utils;
/*
 * The idea is from Spiget but i tried to write it myself:
 * https://github.com/InventivetalentDev/Spiget-Update/blob/6879dfdc0cabecc60b446205d096e434f53de2dd/Core/src/main/java/org/inventivetalent/update/spiget/comparator/VersionComparator.java#L84
 * and if its not unequal enough i put the copyright disclaimer down below!!!
 */
/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;

import java.util.logging.Level;

public abstract class VersionComparator {

    public static final VersionComparator EQUALVERSIONS = new VersionComparator() {
        @Override
        public boolean updateAvailable(String pluginVersionString, String spigotVersionString) {
            return !spigotVersionString.equals(pluginVersionString);
        }
    };

    public static final VersionComparator SEMATIC_VERSION = new VersionComparator() {
        @Override
        public boolean updateAvailable(String pluginVersionString, String spigotVersionString) {
            spigotVersionString = spigotVersionString.replace(".", "");
            pluginVersionString = pluginVersionString.replace(".", "");

            boolean available;
            try {
                //System.out.println("Versions: S:" + spigotVersionString + " | P:" + pluginVersionString);
                if(pluginVersionString.length() != spigotVersionString.length()) {
                    if (pluginVersionString.length() < spigotVersionString.length()) {
                        StringBuilder pluginVersionStringBuilder = new StringBuilder(pluginVersionString);
                        int index = pluginVersionStringBuilder.length();
                        while(index < spigotVersionString.length()) {
                            pluginVersionStringBuilder.append("0");
                            index++;
                        }
                        pluginVersionString = pluginVersionStringBuilder.toString();
                        //System.out.println("Versions Between: S:" + spigotVersionString + " | P:" + pluginVersionStringBuilder);
                    } else {
                        StringBuilder spigotVersionStringBuilder = new StringBuilder(spigotVersionString);
                        int index = spigotVersionStringBuilder.length();
                        while(index < pluginVersionString.length()) {
                            spigotVersionStringBuilder.append("0");
                            index++;
                        }
                        spigotVersionString = spigotVersionStringBuilder.toString();
                        //System.out.println("Versions Between: S:" + spigotVersionStringBuilder + " | P:" + pluginVersionString);
                    }
                }
                //System.out.println("Versions: S:" + spigotVersionString + " | P:" + pluginVersionString);
                int pluginVersion = Integer.parseInt(pluginVersionString);
                int spigotVersion = Integer.parseInt(spigotVersionString);

                available = pluginVersion < spigotVersion;
            } catch (NumberFormatException e) {
                //e.printStackTrace();
                available = false;
                AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE, "generated an Exception: " + e + "(Messages: " + e.getMessage() + ")", LogPrefix.UPDATER);
            }
            return available;
        }
    };

    public abstract boolean updateAvailable(String pluginVersionString, String spigotVersionString);
}
