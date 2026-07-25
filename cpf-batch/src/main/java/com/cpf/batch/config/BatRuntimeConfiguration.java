package com.cpf.batch.config;

import com.cpf.batch.runtime.*;
import com.cpf.core.api.batch.CpfBatchEventPublisher;
import com.cpf.core.common.logging.TransactionIdGenerator;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import com.cpf.core.common.logging.policy.LogPolicyResolver;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.Map;

/**
 * BAT Owner Runtime 구성. Core의 legacy Batch AutoConfiguration을 BAT에서는 비활성화하고
 * Repository/Lock/Heartbeat/Listener/Launcher/Ghost/FileLog를 BAT module이 직접 소유합니다.
 */
@Configuration(proxyBeanMethods=false)
public class BatRuntimeConfiguration {
    @Bean @Primary
    public CpfBatchEventPublisher batBatchEventPublisher(){return new BatBatchLoggingEventPublisher();}

    @Bean
    public BatBatchOperationRepository batBatchOperationRepository(
            @Qualifier("batJdbcTemplate") ObjectProvider<JdbcTemplate> jdbc,
            @Qualifier("batDataSource") ObjectProvider<DataSource> ds,
            CpfFileLogWriter fileLogWriter){return new BatBatchOperationRepository(jdbc,ds,fileLogWriter);}

    @Bean
    public BatBatchLockManager batBatchLockManager(
            @Qualifier("batJdbcTemplate") ObjectProvider<JdbcTemplate> jdbc,
            @Qualifier("batDataSource") ObjectProvider<DataSource> ds){return new BatBatchLockManager(jdbc,ds);}

    @Bean
    public BatBatchHeartbeatService batBatchHeartbeatService(
            BatBatchOperationRepository repo,
            @Value("${cpf.batch.worker.heartbeat-interval-seconds:5}") int interval,
            @Value("${cpf.batch.worker.heartbeat-timeout-seconds:30}") int timeout){return new BatBatchHeartbeatService(repo,interval,timeout);}

    @Bean
    public BatBatchFileLogWriter batBatchFileLogWriter(
            CpfFileLogWriter fileLogWriter, TransactionIdGenerator transactionIdGenerator,
            BatBatchLockManager lockManager,
            @Value("${cpf.batch.file-log.writer-lease-seconds:30}") int leaseSeconds){
        return new BatBatchFileLogWriter(fileLogWriter,transactionIdGenerator,Clock.system(fileLogWriter.logZoneId()),lockManager,leaseSeconds);
    }

    @Bean
    public BatBatchRuntimeListener batBatchRuntimeListener(
            BatBatchHeartbeatService heartbeat,
            ObjectProvider<LogPolicyResolver> logPolicyResolver,
            ObjectProvider<BatBatchFileLogWriter> fileLogWriter){return new BatBatchRuntimeListener(heartbeat,logPolicyResolver,fileLogWriter);}

    @Bean
    public BatBatchGhostDetectionService batBatchGhostDetectionService(BatBatchOperationRepository repo,BatBatchHeartbeatService heartbeat){
        return new BatBatchGhostDetectionService(repo,heartbeat);
    }

    @Bean
    public BatBatchLauncher batBatchLauncher(
            ObjectProvider<JobLauncher> launcher,ObjectProvider<JobExplorer> explorer,ObjectProvider<JobOperator> operator,
            ObjectProvider<Map<String,Job>> jobs,ObjectProvider<CpfBatchEventPublisher> publisher,
            BatBatchOperationRepository repo,BatBatchLockManager lock,
            @Value("${cpf.batch.lock-ttl-seconds:600}") int lockTtlSeconds){
        return new BatBatchLauncher(launcher,explorer,operator,jobs,publisher,repo,lock,lockTtlSeconds);
    }
}
