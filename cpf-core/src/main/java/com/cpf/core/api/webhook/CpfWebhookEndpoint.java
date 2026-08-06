package com.cpf.core.api.webhook;
import java.net.URI; import java.util.Set;
public record CpfWebhookEndpoint(String endpointId,URI uri,String secretVersion,Set<String> allowedEventTypes,boolean enabled,long version){
 public CpfWebhookEndpoint{if(endpointId==null||endpointId.isBlank()||uri==null||secretVersion==null||secretVersion.isBlank()||version<1)throw new IllegalArgumentException("invalid webhook endpoint");allowedEventTypes=Set.copyOf(allowedEventTypes);}
}
