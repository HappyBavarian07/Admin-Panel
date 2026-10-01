package de.happybavarian07.adminpanel.worlds;

import java.util.Locale;
import java.util.Map;

/**
 * Groups the gamerules for the filter button of the gamerule menu. Rules this list does not know, for example rules
 * added by newer versions, land in {@link #OTHER}.
 */
public enum GameRuleCategory {
    MOBS, PLAYER, WORLD, DAMAGE, OTHER;

    private static final Map<String, GameRuleCategory> BY_NAME = Map.ofEntries(
            Map.entry("mobgriefing", MOBS), Map.entry("domobspawning", MOBS), Map.entry("domobloot", MOBS),
            Map.entry("dopatrolspawning", MOBS), Map.entry("dotraderspawning", MOBS), Map.entry("dowardenspawning", MOBS),
            Map.entry("disableraids", MOBS), Map.entry("doinsomnia", MOBS), Map.entry("forgivedeadplayers", MOBS),
            Map.entry("universalanger", MOBS), Map.entry("maxentitycramming", MOBS), Map.entry("mobexplosiondropdecay", MOBS),
            Map.entry("keepinventory", PLAYER), Map.entry("naturalregeneration", PLAYER), Map.entry("doimmediaterespawn", PLAYER),
            Map.entry("showdeathmessages", PLAYER), Map.entry("announceadvancements", PLAYER),
            Map.entry("playerssleepingpercentage", PLAYER), Map.entry("spawnradius", PLAYER),
            Map.entry("spectatorsgeneratechunks", PLAYER), Map.entry("disableelytramovementcheck", PLAYER),
            Map.entry("dolimitedcrafting", PLAYER), Map.entry("enderpearlsvanishondeath", PLAYER),
            Map.entry("drowningdamage", DAMAGE), Map.entry("falldamage", DAMAGE), Map.entry("firedamage", DAMAGE),
            Map.entry("freezedamage", DAMAGE),
            Map.entry("dodaylightcycle", WORLD), Map.entry("doweathercycle", WORLD), Map.entry("dofiretick", WORLD),
            Map.entry("randomtickspeed", WORLD), Map.entry("dotiledrops", WORLD), Map.entry("doentitydrops", WORLD),
            Map.entry("snowaccumulationheight", WORLD), Map.entry("watersourceconversion", WORLD),
            Map.entry("lavasourceconversion", WORLD), Map.entry("dovinesspread", WORLD),
            Map.entry("blockexplosiondropdecay", WORLD), Map.entry("tntexplosiondropdecay", WORLD),
            Map.entry("globalsoundevents", WORLD), Map.entry("tntexplodes", WORLD));

    public static GameRuleCategory of(String ruleName) {
        return BY_NAME.getOrDefault(ruleName.toLowerCase(Locale.ROOT), OTHER);
    }
}
