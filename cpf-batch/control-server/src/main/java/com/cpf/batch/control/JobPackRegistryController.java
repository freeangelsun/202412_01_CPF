package com.cpf.batch.control;

import com.cpf.batch.api.JobPackManifest;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.core.common.database.CpfVendorSqlCatalog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/batch/job-packs")
public class JobPackRegistryController {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final CpfVendorSqlCatalog sql;

    public JobPackRegistryController(
            JdbcTemplate jdbc,
            ObjectMapper mapper,
            Environment environment) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.sql = CpfVendorSqlCatalog.create(environment, "bat");
    }

    @PostMapping("/registrations")
    public ResponseEntity<Void> register(@RequestBody JobPackManifest manifest) throws Exception {
        String json = SensitiveTextSanitizer.sanitize(mapper.writeValueAsString(manifest));
        jdbc.update(sql.required("jobpack-upsert"),
                manifest.jobPackId(),
                manifest.ownerDomain(),
                manifest.artifactCoordinate(),
                manifest.version(),
                manifest.checksum(),
                manifest.signatureBase64() == null ? "N" : "Y",
                manifest.requiredPlatformRange(),
                json);
        jdbc.update(sql.required("jobpack-delete-jobs"), manifest.jobPackId());
        for (var job : manifest.jobs()) {
            jdbc.update(sql.required("jobpack-insert-job"),
                    manifest.jobPackId(),
                    job.jobId(),
                    job.restartable() ? "Y" : "N",
                    job.centerCutProviderKey(),
                    job.centerCutHandlerKey());
        }
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return jdbc.queryForList(sql.required("jobpack-list"));
    }
}
