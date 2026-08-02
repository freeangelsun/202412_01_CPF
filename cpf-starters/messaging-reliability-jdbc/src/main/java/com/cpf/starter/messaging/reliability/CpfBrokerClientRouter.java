package com.cpf.starter.messaging.reliability;
import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public final class CpfBrokerClientRouter implements CpfBrokerClient {
 private final Map<String,CpfNamedBrokerClient> bindings; private final CpfNamedBrokerClient defaultBinding;
 public CpfBrokerClientRouter(List<CpfNamedBrokerClient> clients){
  Map<String,CpfNamedBrokerClient> map=new LinkedHashMap<>();for(var c:clients){if(map.putIfAbsent(c.name(),c)!=null)throw new IllegalStateException("Duplicate broker binding: "+c.name());}
  List<CpfNamedBrokerClient> defaults=clients.stream().filter(CpfNamedBrokerClient::defaultBinding).toList();
  if(clients.size()>1&&defaults.size()!=1)throw new IllegalStateException("Multiple broker providers require exactly one named default binding");
  this.bindings=Map.copyOf(map);this.defaultBinding=defaults.isEmpty()?(clients.size()==1?clients.getFirst():null):defaults.getFirst();
 }
 public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request){if(defaultBinding==null)throw new IllegalStateException("No default broker binding");return defaultBinding.client().enqueue(request);}
 public CpfBrokerPublishResult enqueue(String binding,CpfBrokerPublishRequest request){var client=bindings.get(binding);if(client==null)throw new IllegalStateException("Unknown broker binding: "+binding);return client.client().enqueue(request);}
 public Map<String,String> providers(){Map<String,String> result=new LinkedHashMap<>();bindings.forEach((k,v)->result.put(k,v.provider()));return Map.copyOf(result);}
}
