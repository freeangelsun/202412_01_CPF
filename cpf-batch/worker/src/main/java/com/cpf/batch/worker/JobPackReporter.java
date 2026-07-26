package com.cpf.batch.worker;
import com.cpf.batch.runtime.JobPackCatalog;import org.springframework.beans.factory.annotation.Value;import org.springframework.context.SmartLifecycle;import org.springframework.web.client.RestClient;
import java.util.concurrent.atomic.AtomicBoolean;
public class JobPackReporter implements SmartLifecycle {
 private final JobPackCatalog catalog;private final RestClient client;private final AtomicBoolean running=new AtomicBoolean();
 public JobPackReporter(JobPackCatalog c,RestClient.Builder b,String u){catalog=c;client=b.baseUrl(u).build();}
 public void start(){running.set(true);for(var m:catalog.manifests())try{client.post().uri("/api/v1/batch/job-packs/registrations").body(m).retrieve().toBodilessEntity();}catch(RuntimeException ignored){}}
 public void stop(){running.set(false);}public boolean isRunning(){return running.get();}
}
