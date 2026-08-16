package com.cpf.education.batch.support.integrated;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * 실제 별도 JVM에서 Batch A→B→C 흐름을 수행하는 Runtime worker다.
 * 외부 Harness가 이 프로세스를 강제 종료하여 durable checkpoint/restart와 다중 인스턴스 lease를 검증한다.
 */
public final class BatchAbcProcessWorker {
    public static void main(String[] args) throws Exception {
        if (args.length < 6) {
            System.err.println("usage: <stateDir> <transactionId> <executionId> <attempt> <owner> <holdAfterCheckpoint:true|false>");
            System.exit(64);
        }
        Path stateDir = Path.of(args[0]).toAbsolutePath().normalize();
        String tx = args[1];
        String execution = args[2];
        int attempt = Integer.parseInt(args[3]);
        String owner = args[4];
        boolean holdAfterCheckpoint = Boolean.parseBoolean(args[5]);
        Files.createDirectories(stateDir);
        Path marker = stateDir.resolve("checkpoint.marker");
        Path leasePath = stateDir.resolve("job.lock");

        try (FileChannel leaseChannel = FileChannel.open(leasePath,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            java.nio.channels.FileLock candidate;
            try { candidate = leaseChannel.tryLock(); }
            // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
            catch (OverlappingFileLockException ex) { candidate = null; }
            if (candidate == null) {
                System.out.println("[CPF][EDU][BATCH][LEASE_BUSY] owner=" + owner);
                System.exit(75);
                return;
            }
            try (java.nio.channels.FileLock lock = candidate) {
                run(stateDir, marker, tx, execution, attempt, owner, holdAfterCheckpoint);
            }
        }
    }

    private static void run(Path stateDir, Path marker, String tx, String execution,
                            int attempt, String owner, boolean holdAfterCheckpoint) throws IOException {
        var store = new BatchAbcEducationFlow.FileStore(stateDir.resolve("store.properties"));
        var remote = new BatchAbcEducationFlow.FileRemote(stateDir.resolve("remote.properties"));
        var localLease = new BatchAbcEducationFlow.Lease();
        var domainC = new BatchAbcEducationFlow.DomainC(remote);
        var domainB = new BatchAbcEducationFlow.DomainB(store);
        var domainA = new BatchAbcEducationFlow.DomainA(domainB, domainC);
        var step = new BatchAbcEducationFlow.Step(store, domainA, 1, 1);
        if (holdAfterCheckpoint) {
            step.afterCheckpointHook = () -> {
                try {
                    if (!Files.exists(marker)) {
                        Files.writeString(marker, "checkpoint=" + store.checkpoint() + "\n",
                                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        System.out.println("[CPF][EDU][BATCH][CHECKPOINT_READY] checkpoint=" + store.checkpoint());
                        System.out.flush();
                        Thread.sleep(120_000L);
                    }
                // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new BatchAbcEducationFlow.ProcessKilled();
                } catch (IOException io) {
                    throw new IllegalStateException("checkpoint-marker", io);
                }
            };
        }
        var operator = new BatchAbcEducationFlow.SchedulerOperator(new BatchAbcEducationFlow.Job(step, localLease));
        var identity = new BatchAbcEducationFlow.Identity(tx, "EDU-BATCH-JOB", execution, "EDU-BATCH-STEP", attempt, 0);
        var items = List.of(
                new BatchAbcEducationFlow.Item("K1", "one"),
                new BatchAbcEducationFlow.Item("K2", "two"),
                new BatchAbcEducationFlow.Item("K3", "three"));
        var result = operator.launch(identity, items, owner);
        System.out.println("[CPF][EDU][BATCH][RESULT] state=" + result.state()
                + " checkpoint=" + result.checkpoint()
                + " committed=" + result.committed()
                + " remoteEffects=" + result.remoteEffects()
                + " tx=" + result.identity().transactionId()
                + " execution=" + result.identity().executionId()
                + " attempt=" + result.identity().attempt());
        System.out.flush();
        if (result.state() != BatchAbcEducationFlow.State.SUCCESS) System.exit(2);
    }

    private BatchAbcProcessWorker() {}
}
