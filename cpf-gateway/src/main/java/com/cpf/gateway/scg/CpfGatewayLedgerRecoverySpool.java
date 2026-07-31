package com.cpf.gateway.scg;

import com.cpf.core.api.gateway.CpfGatewayLedgerPort;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 응답을 오염시키지 않으면서 Ledger 실패를 bounded durable spool에 보존·재처리합니다. */
@Component
public final class CpfGatewayLedgerRecoverySpool {
    private static final Logger log=LoggerFactory.getLogger(CpfGatewayLedgerRecoverySpool.class);
    private final CpfGatewayLedgerPort ledger; private final ObjectMapper mapper; private final Path directory; private final long capBytes;
    private final ReentrantLock spoolLock = new ReentrantLock();
    public CpfGatewayLedgerRecoverySpool(CpfGatewayLedgerPort ledger,ObjectMapper mapper,CpfGatewaySafetyProperties properties){this.ledger=ledger;this.mapper=mapper;this.directory=Path.of(properties.getLogSpoolDirectory()).toAbsolutePath().normalize().resolve("ledger-recovery");this.capBytes=properties.getLogSpoolBytesCap();}
    public void begin(CpfGatewayLedgerPort.TransactionStart event){try{ledger.begin(event);}catch(RuntimeException failure){spool("BEGIN",event,failure);}}
    public void recordAttempt(CpfGatewayLedgerPort.Attempt event){try{ledger.recordAttempt(event);}catch(RuntimeException failure){spool("ATTEMPT",event,failure);}}
    public void complete(CpfGatewayLedgerPort.TransactionCompletion event){try{ledger.complete(event);}catch(RuntimeException failure){spool("COMPLETE",event,failure);}}

    @Scheduled(fixedDelayString="${cpf.gateway.ledger-recovery-interval-ms:5000}")
    public void replay(){
        if(!Files.isDirectory(directory))return;
        try(var files=Files.list(directory)){
            files.filter(path->path.getFileName().toString().endsWith(".json")).sorted(Comparator.comparing(Path::toString)).limit(100).forEach(this::replayOne);
        }catch(IOException failure){log.error("CPF Gateway ledger recovery scan failed",failure);}
    }
    private void replayOne(Path path){
        try{
            RecoveryEvent event=mapper.readValue(path.toFile(),RecoveryEvent.class);
            if("BEGIN".equals(event.type()))ledger.begin(mapper.readValue(event.payloadJson(),CpfGatewayLedgerPort.TransactionStart.class));
            else if("ATTEMPT".equals(event.type()))ledger.recordAttempt(mapper.readValue(event.payloadJson(),CpfGatewayLedgerPort.Attempt.class));
            else if("COMPLETE".equals(event.type()))ledger.complete(mapper.readValue(event.payloadJson(),CpfGatewayLedgerPort.TransactionCompletion.class));
            else throw new IllegalStateException("Unsupported recovery event type");
            Files.deleteIfExists(path);
        }catch(Exception failure){log.warn("CPF Gateway ledger recovery retry failed: {}",path.getFileName(),failure);}
    }
    private void spool(String type,Object payload,RuntimeException original){
        Path temporary = null;
        spoolLock.lock();
        try{
            Files.createDirectories(directory);
            long current;
            try(var files=Files.list(directory)){current=files.filter(Files::isRegularFile).mapToLong(this::sizeQuietly).sum();}
            String payloadJson=mapper.writeValueAsString(payload);
            Instant recordedAt=Instant.now();
            RecoveryEvent event=new RecoveryEvent(1,type,payloadJson,recordedAt,sanitize(original));
            byte[] bytes=(mapper.writeValueAsString(event)+"\n").getBytes(StandardCharsets.UTF_8);
            if(current+bytes.length>capBytes)throw new IllegalStateException("CPF_GATEWAY_LEDGER_RECOVERY_SPOOL_FULL");
            Path target=directory.resolve(fileName(recordedAt,UUID.randomUUID()));
            temporary=Files.createTempFile(directory,"ledger-",".tmp");
            Files.write(temporary,bytes,StandardOpenOption.TRUNCATE_EXISTING);
            try{Files.move(temporary,target,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException ignored){Files.move(temporary,target);}
        }catch(Exception spoolFailure){
            original.addSuppressed(spoolFailure);
            log.error("CPF Gateway ledger failure could not be spooled: {} / {}",sanitize(original),sanitize(spoolFailure));
        }finally{
            if(temporary!=null){
                try{Files.deleteIfExists(temporary);}catch(IOException ignored){/* 원 Ledger 실패가 primary입니다. */}
            }
            spoolLock.unlock();
        }
    }
    private static String fileName(Instant instant,UUID id){return String.format("%013d-%09d-%s.json",instant.toEpochMilli(),instant.getNano(),id);}
    private long sizeQuietly(Path path){try{return Files.size(path);}catch(IOException ignored){return 0;}}
    private static String sanitize(Throwable failure){String value=failure.getClass().getSimpleName()+":"+String.valueOf(failure.getMessage());value=value.replaceAll("(?i)(token|password|secret|authorization)[=: ]+[^,;\\s]+","$1=***");return value.length()>500?value.substring(0,500):value;}
    public record RecoveryEvent(int schemaVersion,String type,String payloadJson,Instant recordedAt,String failureSummary){}
}
