package com.cpf.reference.edu.runtime.consumer.file;
import com.cpf.reference.edu.runtime.application.*;
import com.cpf.reference.edu.runtime.consumer.*;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
/** Atomic file consumer with path traversal protection, checksum and restart-safe replace. */
public final class FileEduBusinessConsumer implements EduBusinessConsumer {
    private final Path root; private final ObjectMapper json;
    public FileEduBusinessConsumer(Path root,ObjectMapper json){this.root=root.toAbsolutePath().normalize();this.json=json;}
    @Override public EduConsumerType type(){return EduConsumerType.FILE;}
    @Override public EduBusinessConsumerResult invoke(EduConsumerBinding b,EduExecutionCommand c,long fence){
        try{
            Path dir=root.resolve(b.requirementId().toLowerCase(Locale.ROOT)).normalize();if(!dir.startsWith(root))throw new EduValidationException("unsafe EDU file root");Files.createDirectories(dir);
            String safe=c.businessKey().replaceAll("[^A-Za-z0-9._-]","_");Path target=dir.resolve(safe+".json").normalize();if(!target.startsWith(dir))throw new EduValidationException("unsafe business key");
            byte[] bytes=json.writeValueAsBytes(Map.of("requirementId",b.requirementId(),"businessKey",c.businessKey(),"dataScope",c.dataScope(),"fencingToken",fence,"payload",c.payload(),"writtenAt",Instant.now().toString()));
            Path tmp=Files.createTempFile(dir,safe+"-",".tmp");Files.write(tmp,bytes,StandardOpenOption.TRUNCATE_EXISTING);try{Files.move(tmp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(tmp,target,StandardCopyOption.REPLACE_EXISTING);}
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            return EduBusinessConsumerResult.completed("FILE_COMMITTED",Map.of("path",root.relativize(target).toString().replace('\','/'),"sha256",hash,"size",bytes.length,"consumer",b.entryPoint()));
        }catch(EduValidationException e){throw e;}catch(Exception e){throw new IllegalStateException("EDU file consumer failed: "+e.getMessage(),e);}
    }
}
