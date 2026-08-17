package com.cpf.integration.api.webhook;
import java.net.URI; import java.util.Set;
/** CpfWebhookEndpoint 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfWebhookEndpoint(String endpointId,URI uri,String secretVersion,Set<String> allowedEventTypes,boolean enabled,long version){
 public CpfWebhookEndpoint{if(endpointId==null||endpointId.isBlank()||uri==null||secretVersion==null||secretVersion.isBlank()||version<1)throw new IllegalArgumentException("invalid webhook endpoint");allowedEventTypes=Set.copyOf(allowedEventTypes);}
}
