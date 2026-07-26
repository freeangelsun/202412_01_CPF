package com.cpf.batch.control.deploy;

import com.cpf.batch.api.ArtifactManifest;
import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CompatibilityService {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public CompatibilityService(JdbcTemplate jdbc, Environment environment) {
        this.jdbc = jdbc;
        this.sql = CpfVendorSqlCatalog.create(environment, "bat");
    }

    public Result evaluate(ArtifactManifest artifact, String environmentId) {
        for (var row : jdbc.queryForList(
                sql.required("deploy-compatibility-required-capabilities"),
                environmentId,
                artifact.coordinate())) {
            String capability =
                    String.valueOf(row.getOrDefault("required_capability", ""));
            if (!capability.isBlank()
                    && !artifact.requiredCapabilities().contains(capability)) {
                return new Result(false, "MISSING_CAPABILITY:" + capability);
            }
        }
        return new Result(true, "COMPATIBLE");
    }

    public record Result(boolean allowed, String reason) {
    }
}
