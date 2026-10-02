package de.happybavarian07.adminpanel.worlds;

import org.bukkit.World;
import org.bukkit.WorldType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldLogicTest {
    @Test
    void namesAreFolderSafe() {
        assertTrue(WorldNames.isValid("survival_2"));
        assertTrue(WorldNames.isValid("world.nether-copy"));
        assertFalse(WorldNames.isValid(""));
        assertFalse(WorldNames.isValid("../etc"));
        assertFalse(WorldNames.isValid("a/b"));
        assertFalse(WorldNames.isValid(".hidden"));
        assertFalse(WorldNames.isValid("trailing."));
        assertFalse(WorldNames.isValid("CON"));
        assertFalse(WorldNames.isValid("nul.txt"));
        assertFalse(WorldNames.isValid("x".repeat(65)));
        assertEquals("my-new-world", WorldNames.normalize("  my new  world "));
    }

    @Test
    void seedsAreNumbersOrHashedText() {
        assertNull(WorldNames.seed("  "));
        assertEquals(-42L, WorldNames.seed("-42"));
        assertEquals((long) "glacier".hashCode(), WorldNames.seed("glacier"));
    }

    @Test
    void environmentIsGuessedFromTheDimensionFolder(@TempDir File container) {
        File nether = new File(container, "n");
        assertTrue(new File(nether, "DIM-1").mkdirs());
        File end = new File(container, "e");
        assertTrue(new File(end, "DIM1").mkdirs());
        File normal = new File(container, "w");
        assertTrue(new File(normal, "region").mkdirs());
        assertTrue(new File(normal, "DIM-1").mkdirs());
        assertEquals(World.Environment.NETHER, WorldFolders.guessEnvironment(nether));
        assertEquals(World.Environment.THE_END, WorldFolders.guessEnvironment(end));
        assertEquals(World.Environment.NORMAL, WorldFolders.guessEnvironment(normal));
        assertEquals(World.Environment.NORMAL, WorldFolders.guessEnvironment(new File(container, "missing")));
    }

    @Test
    void findsOnlyUnloadedWorldFolders(@TempDir File container) throws IOException {
        for (String name : List.of("world", "Lobby", "spare", "plugins")) {
            File folder = new File(container, name);
            assertTrue(folder.mkdir());
            if (!name.equals("plugins")) assertTrue(new File(folder, "level.dat").createNewFile());
        }
        assertEquals(List.of("spare"), WorldFolders.unloaded(container, List.of("world", "lobby")));
    }

    @Test
    void copyLeavesServerBoundFilesBehind(@TempDir File container) throws IOException {
        File from = new File(container, "a");
        File data = new File(from, "region");
        assertTrue(data.mkdirs());
        Files.writeString(new File(from, "level.dat").toPath(), "x");
        Files.writeString(new File(from, "uid.dat").toPath(), "id");
        Files.writeString(new File(from, "session.lock").toPath(), "lock");
        Files.writeString(new File(data, "r.0.0.mca").toPath(), "chunks");

        File to = new File(container, "b");
        WorldFolders.copy(from, to);

        assertTrue(new File(to, "level.dat").isFile());
        assertTrue(new File(to, "region/r.0.0.mca").isFile());
        assertFalse(new File(to, "uid.dat").exists());
        assertFalse(new File(to, "session.lock").exists());

        WorldFolders.delete(to);
        assertFalse(to.exists());
        assertTrue(from.exists());
    }

    @Test
    void gameRulesAreCategorized() {
        assertEquals(GameRuleCategory.PLAYER, GameRuleCategory.of("keepInventory"));
        assertEquals(GameRuleCategory.MOBS, GameRuleCategory.of("doMobSpawning"));
        assertEquals(GameRuleCategory.DAMAGE, GameRuleCategory.of("fallDamage"));
        assertEquals(GameRuleCategory.OTHER, GameRuleCategory.of("someRuleFromTheFuture"));
    }

    @Test
    void gameRulesOfNewerVersionsAreCategorizedByTheirWords() {
        assertEquals(GameRuleCategory.WORLD, GameRuleCategory.of("advance_time"));
        assertEquals(GameRuleCategory.MOBS, GameRuleCategory.of("spawn_wandering_traders"));
        assertEquals(GameRuleCategory.PLAYER, GameRuleCategory.of("immediate_respawn"));
        assertEquals(GameRuleCategory.PLAYER, GameRuleCategory.of("spawnRadius"));
        assertEquals(GameRuleCategory.WORLD, GameRuleCategory.of("spawn_chunk_radius"));
        assertEquals(GameRuleCategory.DAMAGE, GameRuleCategory.of("fire_damage"));
        assertEquals(GameRuleCategory.OTHER, GameRuleCategory.of("commandBlockOutput"));
    }

    @Test
    void gameRulesAndSoundsAreFoundByNameWithoutTheirEnumMethods() {
        assertEquals(org.bukkit.GameRule.KEEP_INVENTORY, GameRules.byName("keepInventory"));
        assertNull(GameRules.byName("noSuchRule"));
        assertEquals("keepInventory", GameRules.nameOf(org.bukkit.GameRule.KEEP_INVENTORY));
        assertEquals(org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                de.happybavarian07.adminpanel.utils.ConstantLookup.find(org.bukkit.Sound.class, "NO_SUCH_SOUND", "ENTITY_PLAYER_LEVELUP"));
        assertNull(de.happybavarian07.adminpanel.utils.ConstantLookup.find(org.bukkit.Sound.class, "NO_SUCH_SOUND"));
    }

    @Test
    void gameRuleClicksChangeBooleansAndIntegers() {
        assertEquals(false, GameRuleEdit.next(true, GameRuleEdit.Step.UP));
        assertEquals(true, GameRuleEdit.next(false, GameRuleEdit.Step.DOWN));
        assertEquals(4, GameRuleEdit.next(3, GameRuleEdit.Step.UP));
        assertEquals(13, GameRuleEdit.next(3, GameRuleEdit.Step.UP_BIG));
        assertEquals(0, GameRuleEdit.next(3, GameRuleEdit.Step.DOWN_BIG));
        assertEquals(Integer.MAX_VALUE, GameRuleEdit.next(Integer.MAX_VALUE, GameRuleEdit.Step.UP));
        assertNull(GameRuleEdit.next("text", GameRuleEdit.Step.UP));
    }

    @Test
    void gameRuleTextMustFitTheType() {
        assertEquals(true, GameRuleEdit.parse(false, "On"));
        assertEquals(false, GameRuleEdit.parse(true, "no"));
        assertNull(GameRuleEdit.parse(true, "maybe"));
        assertEquals(300, GameRuleEdit.parse(3, " 300 "));
        assertNull(GameRuleEdit.parse(3, "-1"));
        assertNull(GameRuleEdit.parse(3, "abc"));
        assertNull(GameRuleEdit.parse(3, "99999999999"));
    }

    @Test
    void timeWrapsAroundTheDay() {
        assertEquals(1000, TimeEdit.add(23000, 2000));
        assertEquals(23000, TimeEdit.add(1000, -2000));
        assertEquals(0, TimeEdit.add(0, 24000));
    }

    @Test
    void timeIsReadAsTicksHoursOrClock() {
        assertEquals(6000, TimeEdit.parse("6000"));
        assertEquals(6000, TimeEdit.parse("30000"));
        assertEquals(6000, TimeEdit.parse("12h"));
        assertEquals(18000, TimeEdit.parse("0:00"));
        assertEquals(500, TimeEdit.parse("6:30"));
        assertEquals(-1, TimeEdit.parse("24h"));
        assertEquals(-1, TimeEdit.parse("6:60"));
        assertEquals(-1, TimeEdit.parse("noon"));
    }

    @Test
    void voidPresetIsAFlatWorldWithoutBlocks() {
        assertEquals(WorldType.FLAT, WorldPreset.VOID.type());
        assertTrue(WorldPreset.VOID.generatorSettings().contains("the_void"));
        assertEquals("", WorldPreset.NORMAL.generatorSettings());
        assertEquals(WorldPreset.NORMAL, WorldPreset.LARGE_BIOMES.next());
    }
}
