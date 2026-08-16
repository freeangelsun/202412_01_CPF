package com.cpf.security.resource;
import com.cpf.core.api.context.CpfContext;
import com.cpf.security.api.annotation.CpfPermission;
import com.cpf.security.api.audit.*;
import java.time.Clock;import java.util.*;
/** @CpfPermission의 fail-closed 순수 정책 평가기입니다. */
public final class CpfPermissionEvaluator {
 private final CpfSecurityAnnotationProperties properties; private final CpfAuthorizationAuditSink audit; private final Clock clock;
 public CpfPermissionEvaluator(CpfSecurityAnnotationProperties p,CpfAuthorizationAuditSink a,Clock c){properties=p;audit=Objects.requireNonNull(a);clock=Objects.requireNonNull(c);}
 public void authorize(CpfPermission rule,boolean authenticated,Set<String> granted,CpfContext ctx){
  if(rule==null||!properties.isEnabled())return;Objects.requireNonNull(ctx,"context");
  String[] required=rule.value(); if(required.length==0)deny(ctx,"PERMISSION","EMPTY_PERMISSION_RULE");
  LinkedHashSet<String> normalized=new LinkedHashSet<>(); for(String r:required){if(r==null||r.isBlank())deny(ctx,"PERMISSION","BLANK_PERMISSION");normalized.add(r.trim());}
  if(!authenticated)deny(ctx,"PERMISSION","UNAUTHENTICATED"); Set<String> actual=granted==null?Set.of():Set.copyOf(granted);
  boolean ok=rule.all()?actual.containsAll(normalized):normalized.stream().anyMatch(actual::contains); if(!ok)deny(ctx,"PERMISSION","DENIED");
  audit.record(event(ctx,"PERMISSION",String.join(",",normalized),true,"GRANTED"));
 }
 private void deny(CpfContext c,String type,String reason){audit.record(event(c,type,null,false,reason));throw new SecurityException("CPF_"+reason);}
 private CpfAuthorizationAuditEvent event(CpfContext c,String type,String action,boolean allowed,String reason){return new CpfAuthorizationAuditEvent(type,action,c.transactionId(),c.executionId(),c.subjectId(),c.actorId(),allowed,reason,clock.instant());}
}
