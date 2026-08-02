
package com.cpf.core.api.database;

import java.nio.file.Path;

/** Public factory for a fail-closed CPF three-vendor SQL catalog. */
public final class CpfVendorSqlCatalogs {
    private CpfVendorSqlCatalogs() {}

    /**
     * Creates a catalog from a deployment-selected, verified Vendor Pack root.
     * The root must contain pack.json and runtime/{module}/repository/*.sql.
     */
    public static CpfVendorSqlCatalog fromPack(
            CpfDatabaseVendor vendor, String moduleCode, Path configuredPackRoot) {
        return com.cpf.core.common.database.CpfVendorSqlCatalog.create(
                vendor, moduleCode, configuredPackRoot);
    }
}
