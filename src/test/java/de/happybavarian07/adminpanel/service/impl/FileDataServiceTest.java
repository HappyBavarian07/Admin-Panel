package de.happybavarian07.adminpanel.service.impl;

import de.happybavarian07.adminpanel.service.api.DataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FileDataServiceTest {

    @Test
    public void testSaveAndLoad() throws Exception {
        Path tmp = Files.createTempFile("fds-test-", ".yml");
        tmp.toFile().deleteOnExit();
        String path = tmp.toAbsolutePath().toString();

        FileDataService svc = new FileDataService(path);
        svc.init().join();

        svc.save("k1", "value1").join();
        String loaded = svc.load("k1", String.class).join();
        Assertions.assertEquals("value1", loaded);

        svc.shutdown().join();
    }

    @Test
    public void testSaveAllAndLoadAll() throws Exception {
        Path tmp = Files.createTempFile("fds-test-all-", ".yml");
        tmp.toFile().deleteOnExit();
        String path = tmp.toAbsolutePath().toString();

        FileDataService svc = new FileDataService(path);
        svc.init().join();

        Map<String, Integer> toSave = new HashMap<>();
        toSave.put("one", 1);
        toSave.put("two", 2);
        svc.saveAll(toSave).join();

        Map<String, Integer> loaded = svc.loadAll(Integer.class).join();
        Assertions.assertEquals(2, loaded.size());
        Assertions.assertEquals(1, loaded.get("one"));
        Assertions.assertEquals(2, loaded.get("two"));

        svc.shutdown().join();
    }

    @Test
    public void testDeleteExistsAndListKeys() throws Exception {
        Path tmp = Files.createTempFile("fds-test-del-", ".yml");
        tmp.toFile().deleteOnExit();
        String path = tmp.toAbsolutePath().toString();

        FileDataService svc = new FileDataService(path);
        svc.init().join();

        svc.save("a", "A").join();
        Assertions.assertTrue(svc.exists("a").join());

        Set<String> keys = svc.listKeys().join();
        Assertions.assertTrue(keys.contains("a"));

        svc.delete("a").join();
        Assertions.assertFalse(svc.exists("a").join());

        svc.shutdown().join();
    }

    @Test
    public void testMigrateToAndPersistence() throws Exception {
        Path srcTmp = Files.createTempFile("fds-src-", ".yml");
        Path tgtTmp = Files.createTempFile("fds-tgt-", ".yml");
        srcTmp.toFile().deleteOnExit();
        tgtTmp.toFile().deleteOnExit();
        String srcPath = srcTmp.toAbsolutePath().toString();
        String tgtPath = tgtTmp.toAbsolutePath().toString();

        FileDataService src = new FileDataService(srcPath);
        FileDataService tgt = new FileDataService(tgtPath);
        src.init().join();
        tgt.init().join();

        src.save("migrateKey", "migrateValue").join();
        src.migrateTo(tgt).join();

        String migrated = tgt.load("migrateKey", String.class).join();
        Assertions.assertEquals("migrateValue", migrated);

        src.shutdown().join();
        tgt.shutdown().join();

        FileDataService reloaded = new FileDataService(tgtPath);
        reloaded.init().join();
        String persisted = reloaded.load("migrateKey", String.class).join();
        Assertions.assertEquals("migrateValue", persisted);
        reloaded.shutdown().join();
    }
}

