package com.cpf.security.resource;
import com.cpf.core.api.context.CpfContext;
import com.cpf.security.api.annotation.CpfApprovalRequired;
import com.cpf.security.api.approval.*;
import com.cpf.security.api.audit.*;
import java.lang.reflect.*;import java.time.Clock;import java.util.*;
/** @CpfApprovalRequired 입력 해석과 Owner verifier 호출을 fail-closed로 수행합니다. */
public final class CpfApprovalCoordinator {
 private final CpfSecurityAnnotationProperties properties; private final CpfApprovalVerifier verifier; private final CpfAuthorizationAuditSink audit; private final Clock clock;
 public CpfApprovalCoordinator(CpfSecurityAnnotationProperties p,CpfApprovalVerifier v,CpfAuthorizationAuditSink a,Clock c){properties=p;verifier=v;audit=Objects.requireNonNull(a);clock=Objects.requireNonNull(c);}
 public void authorize(Method method,Object[] args,CpfApprovalRequired rule,CpfContext ctx){
  if(rule==null||!properties.isEnabled())return;Objects.requireNonNull(ctx,"context");
  if(rule.action().isBlank()||rule.approvals()<1)deny(ctx,rule.action(),"APPROVAL_RULE_INVALID");
  if(verifier==null)deny(ctx,rule.action(),"APPROVAL_VERIFIER_MISSING");
  Object[] values=args==null?new Object[0]:args; Long approvalId=number(value(method,values,rule.approvalIdParameterIndex(),rule.approvalIdParameter()));
  String reason=text(value(method,values,rule.reasonParameterIndex(),rule.reasonParameter()));
  if(approvalId==null||approvalId<=0)deny(ctx,rule.action(),"APPROVAL_ID_MISSING");
  long verifiedApprovalId=Objects.requireNonNull(approvalId,"approvalId").longValue();
  if(rule.reasonRequired()&&(reason==null||reason.isBlank()))deny(ctx,rule.action(),"APPROVAL_REASON_MISSING");
  try{verifier.verify(new CpfApprovalVerification(verifiedApprovalId,rule.action(),rule.approvals(),ctx.actorId(),reason,ctx.transactionId()));}
  catch(RuntimeException e){audit.record(event(ctx,rule.action(),false,"APPROVAL_REJECTED"));throw e;}
  audit.record(event(ctx,rule.action(),true,"APPROVED"));
 }
 private static Object value(Method m,Object[] args,int index,String name){
  if(index>=0){if(index>=args.length)throw new SecurityException("CPF_APPROVAL_PARAMETER_INDEX_INVALID");return args[index];}
  Parameter[] ps=m.getParameters(); for(int i=0;i<Math.min(ps.length,args.length);i++)if(ps[i].isNamePresent()&&ps[i].getName().equals(name))return args[i];
  throw new SecurityException("CPF_APPROVAL_PARAMETER_UNRESOLVED");
 }
 private static Long number(Object v){return v instanceof Number n?n.longValue():null;} private static String text(Object v){return v==null?null:String.valueOf(v).trim();}
 private void deny(CpfContext c,String action,String reason){audit.record(event(c,action,false,reason));throw new SecurityException("CPF_"+reason);}
 private CpfAuthorizationAuditEvent event(CpfContext c,String action,boolean allowed,String reason){return new CpfAuthorizationAuditEvent("APPROVAL",action,c.transactionId(),c.executionId(),c.subjectId(),c.actorId(),allowed,reason,clock.instant());}
}
