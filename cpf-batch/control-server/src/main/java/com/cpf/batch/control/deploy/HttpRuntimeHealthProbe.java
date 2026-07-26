package com.cpf.batch.control.deploy;
import com.cpf.batch.api.DeploymentCellManifest;import com.cpf.batch.spi.RuntimeHealthProbe;
import org.springframework.stereotype.Component;import org.springframework.web.client.RestClient;
@Component public class HttpRuntimeHealthProbe implements RuntimeHealthProbe {
 private final RestClient.Builder builder;public HttpRuntimeHealthProbe(RestClient.Builder builder){this.builder=builder;}
 public Health probe(DeploymentCellManifest.Instance i,String path,int timeoutSeconds){
  try{String body=builder.baseUrl("http://"+i.hostAlias()+":"+i.port()).build().get().uri(path).retrieve().body(String.class);boolean ok=body!=null&&(body.toUpperCase().contains("UP")||body.contains("\"ready\":true"));return new Health(ok,ok,ok?"UP":"NOT_READY");}
  catch(RuntimeException e){return new Health(false,false,e.getClass().getSimpleName());}
 }
}
