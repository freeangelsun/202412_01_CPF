package com.cpf.data.persistence.sql;

import com.cpf.data.persistence.api.database.CpfDatabaseVendor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfVendorResourceRootTest {
    @TempDir
    Path root;

    @Test
    void acceptsExactlyTheCurrentOfficialPackSchema() throws IOException {
        writeManifest(5);
        assertEquals(root.toRealPath(),
                CpfVendorResourceRoot.required(root, CpfDatabaseVendor.MARIADB));
    }

    @Test
    void rejectsRetiredAndUnknownPackSchemas() throws IOException {
        for (int version : new int[] {1, 4, 6}) {
            writeManifest(version);
            IllegalStateException failure = assertThrows(IllegalStateException.class,
                    () -> CpfVendorResourceRoot.required(root, CpfDatabaseVendor.MARIADB));
            org.junit.jupiter.api.Assertions.assertTrue(
                    failure.getMessage().contains("expected=5, actual=" + version));
        }
    }

    private void writeManifest(int schemaVersion) throws IOException {
        Files.writeString(root.resolve("pack.json"), """
                {"schemaVersion":%d,"vendor":"mariadb","status":"완료"}
                """.formatted(schemaVersion));
    }
}
