package com.cpf.batch.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

@Component
public class WorkerRegistryReporter {
    private final WorkerRuntime runtime; private final JdbcTemplate jdbc; private final ObjectMapper mapper;
    private final String serverInstanceId;
    public WorkerRegistryReporter(WorkerRuntime runtime,JdbcTemplate jdbc,ObjectMapper mapper,
      @Value("${cpf.framework.was-id:${CPF_WAS_ID:batWK-local-01}}") String serverInstanceId) {
        this.runtime=runtime;this.jdbc=jdbc;this.mapper=mapper;this.serverInstanceId=serverInstanceId;
    }
    @Scheduled(fixedDelayString="${cpf.batch.worker.registry-heartbeat-ms:5000}",initialDelayString="${cpf.batch.worker.registry-initial-delay-ms:1000}")
    public void heartbeat() {
        String host; try{host=InetAddress.getLocalHost().getHostName();}catch(Exception e){host="unknown";}
        String pid=ManagementFactory.getRuntimeMXBean().getName();
        String caps; try{caps=mapper.writeValueAsString(runtime.capabilities());}catch(JsonProcessingException e){caps="[]";}
        String state=runtime.draining()?"DRAINING":(runtime.currentExecutionId()==null?"IDLE":"RUNNING");
        jdbc.update("""
          INSERT INTO bat_worker(worker_id,server_instance_id,host_name,process_id,worker_version,capabilities_json,
            max_concurrency,queue_capacity,control_status,worker_status,active_yn,last_heartbeat_at,current_execution_id,created_by,updated_by)
          VALUES(?,?,?,?,?,?,?,1,?,?, 'Y',CURRENT_TIMESTAMP(3),?,'BAT','BAT')
          ON DUPLICATE KEY UPDATE server_instance_id=VALUES(server_instance_id),host_name=VALUES(host_name),process_id=VALUES(process_id),
            worker_version=VALUES(worker_version),capabilities_json=VALUES(capabilities_json),max_concurrency=VALUES(max_concurrency),
            control_status=VALUES(control_status),worker_status=VALUES(worker_status),active_yn='Y',last_heartbeat_at=CURRENT_TIMESTAMP(3),
            current_execution_id=VALUES(current_execution_id),updated_by='BAT',updated_at=CURRENT_TIMESTAMP
          """,runtime.workerId(),serverInstanceId,host,pid,runtime.workerVersion(),caps,runtime.maxConcurrency(),
          runtime.draining()?"DRAINING":"RUNNING",state,runtime.currentExecutionId());
    }
}
