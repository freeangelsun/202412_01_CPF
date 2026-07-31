package com.cpf.batch.control.deploy;
import com.cpf.batch.api.DeploymentCellManifest;import com.cpf.batch.spi.RuntimeHealthProbe;import java.time.Duration;import java.util.Map;import org.springframework.http.client.JdkClientHttpRequestFactory;import org.springframework.stereotype.Component;import org.springframework.web.client.RestClient;
@Component public class HttpRuntimeHealthProbe implements RuntimeHealthProbe {
 private final RestClient.Builder builder;public HttpRuntimeHealthProbe(RestClient.Builder builder){this.builder=builder;}
 public Health probe(DeploymentCellManifest.Instance i,String path,int timeoutSeconds){
  int timeout=Math.max(1,Math.min(timeoutSeconds,120));try{var f=new JdkClientHttpRequestFactory();f.setReadTimeout(Duration.ofSeconds(timeout));RestClient client=builder.requestFactory(f).baseUrl("http://"+i.hostAlias()+":"+i.port()).build();Map<?,?> body=client.get().uri(path).retrieve().body(Map.class);String status=String.valueOf(body==null?null:body.get("status"));Object ready=body==null?null:body.get("ready");boolean up="UP".equals(status)&&Boolean.TRUE.equals(ready);return new Health(up,up,up?"UP_READY":"STATUS="+status+",ready="+ready);}
  catch(RuntimeException e){return new Health(false,false,"PROBE_"+e.getClass().getSimpleName());}
 }
}
