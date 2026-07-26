package com.cpf.batch.control;
import com.cpf.batch.api.JobPackManifest;import com.cpf.batch.runtime.SensitiveTextSanitizer;import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/batch/job-packs")
public class JobPackRegistryController {
 private final JdbcTemplate jdbc;private final ObjectMapper mapper;public JobPackRegistryController(JdbcTemplate j,ObjectMapper m){jdbc=j;mapper=m;}
 @PostMapping("/registrations") public ResponseEntity<Void> register(@RequestBody JobPackManifest m)throws Exception{
  String json=SensitiveTextSanitizer.sanitize(mapper.writeValueAsString(m));
  jdbc.update("""
   INSERT INTO bat_job_pack(job_pack_id,owner_domain,artifact_coordinate,artifact_version,artifact_checksum,signature_present_yn,platform_range,manifest_json,last_registered_at)
   VALUES(?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP(6))
   ON DUPLICATE KEY UPDATE owner_domain=VALUES(owner_domain),artifact_coordinate=VALUES(artifact_coordinate),artifact_version=VALUES(artifact_version),artifact_checksum=VALUES(artifact_checksum),signature_present_yn=VALUES(signature_present_yn),platform_range=VALUES(platform_range),manifest_json=VALUES(manifest_json),last_registered_at=CURRENT_TIMESTAMP(6)
   """,
   m.jobPackId(),m.ownerDomain(),m.artifactCoordinate(),m.version(),m.checksum(),m.signatureBase64()==null?"N":"Y",m.requiredPlatformRange(),json);
  jdbc.update("DELETE FROM bat_job_pack_job WHERE job_pack_id=?",m.jobPackId());
  for(var j:m.jobs())jdbc.update("INSERT INTO bat_job_pack_job(job_pack_id,job_id,restartable_yn,center_cut_provider_key,center_cut_handler_key) VALUES(?,?,?,?,?)",m.jobPackId(),j.jobId(),j.restartable()?"Y":"N",j.centerCutProviderKey(),j.centerCutHandlerKey());
  return ResponseEntity.accepted().build();
 }
 @GetMapping public List<Map<String,Object>> list(){return jdbc.queryForList("SELECT job_pack_id,owner_domain,artifact_coordinate,artifact_version,artifact_checksum,signature_present_yn,platform_range,last_registered_at FROM bat_job_pack ORDER BY owner_domain,job_pack_id");}
}
