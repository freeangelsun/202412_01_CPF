package com.cpf.batch.worker;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.runtime.RuntimeStateProvider;
import com.cpf.batch.worker.internal.JdbcWorkerLeaseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 독립 Worker Runtime. maxConcurrency만큼 Lease를 병렬 보유하며 각 실행은 virtual thread에서 수행합니다.
 * Drain 중에는 신규 Claim을 중단하고 현재 실행이 0이 될 때까지 기다립니다.
 */
@Component
public class WorkerRuntime implements RuntimeStateProvider, AutoCloseable {
    private final JdbcWorkerLeaseRepository repository;
    private final JobPackDispatcher dispatcher;
    private final String workerId,workerVersion;
    private final List<String> capabilities;
    private final int maxConcurrency;
    private final Duration leaseDuration;
    private final ExecutorService executor=Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicBoolean draining=new AtomicBoolean();
    private final ConcurrentMap<Long,JdbcWorkerLeaseRepository.Lease> active=new ConcurrentHashMap<>();

    public WorkerRuntime(JdbcWorkerLeaseRepository repository,JobPackDispatcher dispatcher,
      @Value("${cpf.batch.worker.worker-id:${CPF_BAT_WORKER_ID:${CPF_INSTANCE_ID:worker-local-01}}}") String workerId,
      @Value("${cpf.batch.worker.version:${CPF_BAT_WORKER_VERSION:${CPF_ARTIFACT_VERSION:dev}}}") String workerVersion,
      @Value("${cpf.batch.worker.capabilities:${CPF_BAT_WORKER_CAPABILITIES:GENERAL}}") String capabilityText,
      @Value("${cpf.batch.worker.max-concurrency:1}") int maxConcurrency,
      @Value("${cpf.batch.worker.lease-seconds:30}") long leaseSeconds) {
        this.repository=repository;this.dispatcher=dispatcher;this.workerId=workerId;this.workerVersion=workerVersion;
        this.capabilities=Arrays.stream(capabilityText.split(",")).map(String::trim).filter(x->!x.isBlank()).distinct().toList();
        this.maxConcurrency=Math.max(1,maxConcurrency);this.leaseDuration=Duration.ofSeconds(Math.max(10,leaseSeconds));
    }

    @Scheduled(fixedDelayString="${cpf.batch.worker.recovery-ms:5000}")
    public void recoverExpired() { repository.recoverExpired(); }

    @Scheduled(fixedDelayString="${cpf.batch.worker.poll-ms:1000}")
    public void poll() {
        if(draining.get()) return;
        int slots=Math.max(0,maxConcurrency-active.size());
        for(int i=0;i<slots&&!draining.get();i++) {
            Optional<JdbcWorkerLeaseRepository.Lease> claimed=repository.claim(workerId,workerVersion,capabilities,leaseDuration);
            if(claimed.isEmpty()) break;
            JdbcWorkerLeaseRepository.Lease lease=claimed.get();
            if(active.putIfAbsent(lease.executionId(),lease)!=null) continue;
            executor.submit(()->{
                try{dispatcher.execute(lease);}
                finally{active.remove(lease.executionId(),lease);}
            });
        }
    }

    @Scheduled(fixedDelayString="${cpf.batch.worker.heartbeat-ms:5000}")
    public void renew() {
        for(JdbcWorkerLeaseRepository.Lease lease:new ArrayList<>(active.values())) {
            // Lease를 잃은 실행 Thread를 중단할 수 없으므로 active slot은 Thread 종료까지 유지합니다.
            // DB completion은 lease token/fencing/expiry CAS로 차단되고 Recovery가 UNKNOWN_RESULT를 소유합니다.
            repository.renew(lease,leaseDuration);
        }
    }

    public void drain(){draining.set(true);} public void resume(){draining.set(false);}
    public boolean drained(){return draining.get()&&active.isEmpty();}
    public String workerId(){return workerId;} public String workerVersion(){return workerVersion;}
    public List<String> capabilities(){return capabilities;} public int maxConcurrency(){return maxConcurrency;}
    public Long currentExecutionId(){return active.keySet().stream().sorted().findFirst().orElse(null);}
    public ActualState actualState(){return draining.get()?ActualState.DRAINING:(active.isEmpty()?ActualState.READY:ActualState.BUSY);}
    public List<String> currentExecutions(){return active.keySet().stream().sorted().map(String::valueOf).toList();}
    public List<String> activeLeases(){return active.values().stream().map(JdbcWorkerLeaseRepository.Lease::leaseToken).sorted().toList();}
    public int availableCapacity(){return draining.get()?0:Math.max(0,maxConcurrency-active.size());}
    public boolean draining(){return draining.get();}
    public long fencingToken(){return active.values().stream().mapToLong(JdbcWorkerLeaseRepository.Lease::fencingToken).max().orElse(0L);}
    public void close(){draining.set(true);executor.shutdown();}
}
