package de.happybavarian07.adminpanel.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class DataServiceMigrationTest {

    @Test
    public void testFileToSQLiteMigration() throws Exception {
        Path fileTmp = Files.createTempFile("fds-mig-src-", ".yml");
        Path sqliteTmp = Files.createTempFile("sds-mig-tgt-", ".db");
        fileTmp.toFile().deleteOnExit();
        sqliteTmp.toFile().deleteOnExit();
        String filePath = fileTmp.toAbsolutePath().toString();
        String sqlPath = sqliteTmp.toAbsolutePath().toString();

        FileDataService fds = new FileDataService(filePath);
        SQLiteDataService sds = new SQLiteDataService(sqlPath);
        fds.init().join();
        sds.init().join();

        fds.save("alpha", "one").join();
        fds.save("beta", "two").join();

        DataServiceMigrationUtil.migrate(fds, sds, String.class).join();

        String a = sds.load("alpha", String.class).join();
        String b = sds.load("beta", String.class).join();

        Assertions.assertEquals("one", a);
        Assertions.assertEquals("two", b);

        fds.shutdown().join();
        sds.shutdown().join();
    }

    @Test
    public void testSQLiteToFileMigration() throws Exception {
        Path fileTmp = Files.createTempFile("fds-mig-tgt-", ".yml");
        Path sqliteTmp = Files.createTempFile("sds-mig-src-", ".db");
        fileTmp.toFile().deleteOnExit();
        sqliteTmp.toFile().deleteOnExit();
        String filePath = fileTmp.toAbsolutePath().toString();
        String sqlPath = sqliteTmp.toAbsolutePath().toString();

        FileDataService fds = new FileDataService(filePath);
        SQLiteDataService sds = new SQLiteDataService(sqlPath);
        fds.init().join();
        sds.init().join();

        sds.save("gamma", "three").join();
        sds.save("delta", "four").join();

        DataServiceMigrationUtil.migrate(sds, fds, String.class).join();

        String g = fds.load("gamma", String.class).join();
        String d = fds.load("delta", String.class).join();

        Assertions.assertEquals("three", g);
        Assertions.assertEquals("four", d);

        fds.shutdown().join();
        sds.shutdown().join();
    }
}
