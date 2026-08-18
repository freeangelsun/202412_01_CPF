package com.cpf.batch.control.retention;

import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;

/** Due policy poller. Multi-WAS single executor는 DB lease claim이 보장합니다. */
@Component
public class BatRetentionScheduler {
    private static final Logger log=LoggerFactory.getLogger(BatRetentionScheduler.class);
    private final BatRetentionExecutionRepository repository; private final BatRetentionExecutionService service;
    public BatRetentionScheduler(BatRetentionExecutionRepository repository,BatRetentionExecutionService service){this.repository=repository;this.service=service;}
    @Scheduled(fixedDelayString="${cpf.retention.scheduler.poll-ms:30000}")
    public void runDue(){
        for(String id:repository.findDuePolicyIds(Instant.now(),20)){
            try{service.runScheduled(id);}catch(IllegalStateException busy){if(!"RETENTION_LEASE_BUSY".equals(busy.getMessage())&&!"RETENTION_OUTSIDE_MAINTENANCE_WINDOW".equals(busy.getMessage()))log.warn("Retention schedule skipped policy={} error={}",id,busy.getClass().getSimpleName());}
            catch(RuntimeException failure){log.warn("Retention scheduled run failed policy={} error={}",id,failure.getClass().getSimpleName());}
        }
    }
}
