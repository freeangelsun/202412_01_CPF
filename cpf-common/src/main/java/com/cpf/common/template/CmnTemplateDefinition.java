package com.cpf.common.template;
import java.util.Set;
public record CmnTemplateDefinition(String templateCode,long version,String channel,String body,Set<String> allowedVariables,boolean active){public CmnTemplateDefinition{allowedVariables=allowedVariables==null?Set.of():Set.copyOf(allowedVariables);}}
