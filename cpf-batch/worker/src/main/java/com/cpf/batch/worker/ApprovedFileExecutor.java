package com.cpf.batch.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.function.Consumer;

/** Path Alias 경계에서만 File Watch/Process/Transfer를 수행합니다. */
@Component
public class ApprovedFileExecutor {
    private static final Logger log = LoggerFactory.getLogger(ApprovedFileExecutor.class);

    private final WorkerOperationalProperties properties;
    public ApprovedFileExecutor(WorkerOperationalProperties properties) { this.properties=properties; }

    public Path resolve(String alias,String relative) {
        WorkerOperationalProperties.PathAlias cfg=properties.getPathAliases().get(alias);
        if(cfg==null||cfg.getRoot()==null||cfg.getRoot().isBlank())throw new SecurityException("Path alias not approved: "+alias);
        Path root=Path.of(cfg.getRoot()).toAbsolutePath().normalize();
        Path target=root.resolve(Objects.requireNonNull(relative,"relative")).normalize();
        if(!target.startsWith(root))throw new SecurityException("Path escaped alias root");
        return target;
    }

    public boolean sharedDurable(String alias) {
        WorkerOperationalProperties.PathAlias cfg=properties.getPathAliases().get(alias);
        return cfg!=null&&cfg.isSharedDurable();
    }

    public Path await(String alias,String relative,java.time.Duration timeout) throws IOException, InterruptedException, java.util.concurrent.TimeoutException {
        Path target=resolve(alias,relative);
        if(Files.exists(target))return target;
        long deadline=System.nanoTime()+timeout.toNanos();
        Path parent=target.getParent();Files.createDirectories(parent);
        try(WatchService service=parent.getFileSystem().newWatchService()){
            parent.register(service,StandardWatchEventKinds.ENTRY_CREATE);
            while(System.nanoTime()<deadline){
                long remaining=Math.max(1,java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(deadline-System.nanoTime()));
                WatchKey key=service.poll(Math.min(remaining,1000),java.util.concurrent.TimeUnit.MILLISECONDS);
                if(key==null)continue;
                for(WatchEvent<?> event:key.pollEvents()){
                    if(event.kind()==StandardWatchEventKinds.ENTRY_CREATE && target.getFileName().equals(event.context()))return target;
                }
                if(!key.reset())break;
            }
        }
        throw new java.util.concurrent.TimeoutException("File watch timeout: "+alias+"/"+relative);
    }

    public Path transfer(String sourceAlias,String sourceRelative,String targetAlias,String targetRelative,boolean overwrite) throws IOException {
        Path source=resolve(sourceAlias,sourceRelative),target=resolve(targetAlias,targetRelative);
        Files.createDirectories(target.getParent());
        return overwrite?Files.copy(source,target,StandardCopyOption.REPLACE_EXISTING):Files.copy(source,target);
    }

    /** inbox 파일을 processing alias로 원자적으로 이동해 중복 Process를 방지합니다. */
    public Path claimForProcess(String sourceAlias,String relative,String processingAlias) throws IOException {
        Path source=resolve(sourceAlias,relative),target=resolve(processingAlias,relative);
        Files.createDirectories(target.getParent());
        try{return Files.move(source,target,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){return Files.move(source,target);}
    }

    public WatchHandle watch(String alias,Consumer<Path> consumer) throws IOException {
        Path root=resolve(alias,"."); Files.createDirectories(root); WatchService service=root.getFileSystem().newWatchService();
        root.register(service,StandardWatchEventKinds.ENTRY_CREATE);
        Thread thread=Thread.ofVirtual().name("cpf-file-watch-"+alias).start(()->{
            try{
                while(!Thread.currentThread().isInterrupted()){
                    WatchKey key=service.take();
                    for(WatchEvent<?> event:key.pollEvents()) if(event.kind()==StandardWatchEventKinds.ENTRY_CREATE) consumer.accept(root.resolve((Path)event.context()).normalize());
                    if(!key.reset())break;
                }
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }finally{
                closeWatchService(service,alias);
            }
        });
        return ()->{thread.interrupt();closeWatchService(service,alias);};
    }

    private static void closeWatchService(WatchService service,String alias) {
        try {
            service.close();
        } catch (IOException failure) {
            log.warn("File watch close failed. alias={}, cause={}",alias,failure.getClass().getSimpleName());
        }
    }

    public interface WatchHandle extends AutoCloseable { @Override void close(); }
}
