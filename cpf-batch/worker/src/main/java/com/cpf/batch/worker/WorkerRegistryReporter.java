package com.cpf.batch.worker;

import com.cpf.core.common.database.CpfVendorSqlCatalog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

@Component
public class WorkerRegistryReporter {
    private final WorkerRuntime runtime; private final JdbcTemplate jdbc; private final ObjectMapper mapper;
    private final CpfVendorSqlCatalog sql;
    private final String serverInstanceId;
    public WorkerRegistryReporter(WorkerRuntime runtime,JdbcTemplate jdbc,ObjectMapper mapper,
      @Value("${cpf.framework.was-id:${CPF_WAS_ID:batWK-local-01}}") String serverInstanceId,
      Environment environment) {
        this.runtime=runtime;this.jdbc=jdbc;this.mapper=mapper;this.serverInstanceId=serverInstanceId;
        this.sql= CpfVendorSqlCatalog.create(environment, "bat");
    }
    @Scheduled(fixedDelayString="${cpf.batch.worker.registry-heartbeat-ms:5000}",initialDelayString="${cpf.batch.worker.registry-initial-delay-ms:1000}")
    public void heartbeat() {
        String host; try{host=InetAddress.getLocalHost().getHostName();}catch(Exception e){host="unknown";}
        String pid=ManagementFactory.getRuntimeMXBean().getName();
        String caps; try{caps=mapper.writeValueAsString(runtime.capabilities());}catch(JsonProcessingException e){caps="[]";}
        String state=runtime.draining()?"DRAINING":(runtime.currentExecutionId()==null?"IDLE":"RUNNING");
        jdbc.update(sql.required("worker-registry-upsert"),
          runtime.workerId(),serverInstanceId,host,pid,runtime.workerVersion(),caps,runtime.maxConcurrency(),
          runtime.draining()?"DRAINING":"RUNNING",state,runtime.currentExecutionId());
    }
}
