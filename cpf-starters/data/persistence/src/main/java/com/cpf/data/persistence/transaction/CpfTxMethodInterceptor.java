package com.cpf.data.persistence.transaction;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContext.CpfOperationContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.data.persistence.api.annotation.CpfTx;
import java.util.Map;
import java.util.Objects;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ClassUtils;

/** @CpfTx를 named TransactionManager와 CPF Operation Context에 연결합니다. */
final class CpfTxMethodInterceptor implements MethodInterceptor {
 private final Map<String,PlatformTransactionManager> transactionManagers;
 CpfTxMethodInterceptor(Map<String,PlatformTransactionManager> transactionManagers){
  this.transactionManagers=Map.copyOf(Objects.requireNonNull(transactionManagers));
  if(this.transactionManagers.isEmpty())throw new IllegalArgumentException("PlatformTransactionManager bean is required");
 }
 @Override public Object invoke(MethodInvocation invocation) throws Throwable {
  Class<?> targetClass=invocation.getThis()==null?invocation.getMethod().getDeclaringClass():ClassUtils.getUserClass(invocation.getThis());
  CpfTx tx=CpfTxAnnotationResolver.resolve(invocation.getMethod(),targetClass); if(tx==null)return invocation.proceed();
  rejectLegacyDuplicate(invocation,targetClass); validate(tx);
  PlatformTransactionManager manager=resolveManager(tx);
  CpfContextSnapshot current=CpfContexts.requireSnapshot(); CpfContextSnapshot operationSnapshot=withOperation(current,tx);
  DefaultTransactionDefinition def=new DefaultTransactionDefinition(mapPropagation(tx.propagation().value()));
  def.setName(tx.id()); def.setReadOnly(tx.readOnly()); def.setIsolationLevel(tx.isolation().value()); if(tx.timeoutSeconds()>=0) def.setTimeout(tx.timeoutSeconds());
  TransactionTemplate template=new TransactionTemplate(manager,def);
  try { return template.execute(status->{ try(AutoCloseable ignored=CpfContexts.bind(operationSnapshot)){ return invocation.proceed(); }
   catch(RuntimeException|Error ex){throw ex;} catch(Throwable ex){throw new CheckedInvocationException(ex);} }); }
  catch(CheckedInvocationException wrapped){throw wrapped.original;}
 }
 private PlatformTransactionManager resolveManager(CpfTx tx){
  String requested=tx.transactionManager()==null?"":tx.transactionManager().trim();
  if(!requested.isEmpty()){
   PlatformTransactionManager manager=transactionManagers.get(requested);
   if(manager==null)throw new IllegalStateException("@CpfTx transactionManager not found: "+requested+" available="+transactionManagers.keySet());
   return manager;
  }
  if(transactionManagers.size()==1)return transactionManagers.values().iterator().next();
  PlatformTransactionManager conventional=transactionManagers.get("transactionManager");
  if(conventional!=null)return conventional;
  throw new IllegalStateException("@CpfTx transactionManager must be explicit when multiple managers exist: "+transactionManagers.keySet());
 }
 private static int mapPropagation(int value){
  return switch(value){
   case 0->TransactionDefinition.PROPAGATION_REQUIRED; case 1->TransactionDefinition.PROPAGATION_SUPPORTS;
   case 2->TransactionDefinition.PROPAGATION_MANDATORY; case 3->TransactionDefinition.PROPAGATION_REQUIRES_NEW;
   case 4->TransactionDefinition.PROPAGATION_NOT_SUPPORTED; case 5->TransactionDefinition.PROPAGATION_NEVER;
   case 6->TransactionDefinition.PROPAGATION_NESTED; default->throw new IllegalArgumentException("Unsupported propagation value: "+value);
  };
 }
 private static CpfContextSnapshot withOperation(CpfContextSnapshot current,CpfTx tx){
  CpfOperationContext before=current.operation();
  CpfOperationContext next=new CpfOperationContext(tx.id(),tx.name(),before==null?null:before.commandId(),before==null?null:before.idempotencyKey(),before==null?null:before.idempotencyScope(),before==null?null:before.idempotencyMode(),before==null?null:before.payloadFingerprint(),before==null?null:before.operationId(),before==null?1L:before.transactionSequence());
  CpfContext context=current.context().child(current.execution(),next); return CpfContextSnapshot.capture(context,current.capturedAt());
 }
 private static void rejectLegacyDuplicate(MethodInvocation invocation,Class<?> targetClass){
  for(var a:invocation.getMethod().getAnnotations()) if(a.annotationType().getSimpleName().equals("CpfOnlineTransaction")) throw new IllegalStateException("@CpfTx and @CpfOnlineTransaction must not coexist: "+invocation.getMethod());
  if(targetClass!=null) for(var a:targetClass.getAnnotations()) if(a.annotationType().getSimpleName().equals("CpfOnlineTransaction")) throw new IllegalStateException("@CpfTx and @CpfOnlineTransaction must not coexist: "+targetClass.getName());
 }
 private static void validate(CpfTx tx){ if(tx.id().isBlank())throw new IllegalStateException("@CpfTx id must not be blank"); if(tx.name().isBlank())throw new IllegalStateException("@CpfTx name must not be blank"); if(tx.ownerDomain().isBlank())throw new IllegalStateException("@CpfTx ownerDomain must not be blank"); }
 private static final class CheckedInvocationException extends RuntimeException { private final Throwable original; private CheckedInvocationException(Throwable original){super(original);this.original=original;} }
}
