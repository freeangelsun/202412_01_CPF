#!/usr/bin/env python3
# CPF 개발/검증 Source이며 최신 Requirement와 실패 누적 검증 계약을 따릅니다.
from __future__ import annotations
import argparse, json, subprocess, tempfile, textwrap, shutil, os
from pathlib import Path
def source_identity(root: Path) -> str:
    env = os.environ.get("CPF_SOURCE_SHA", "").strip()
    if len(env) == 40 and all(c in "0123456789abcdefABCDEF" for c in env):
        return env.lower()
    if (root / '.git').exists():
        cp = subprocess.run(['git','rev-parse','HEAD'], cwd=root, text=True, capture_output=True)
        value = (cp.stdout or '').strip()
        if cp.returncode == 0 and len(value) == 40:
            return value
    base = root / 'cpf-docs/work/BASE_SHA.txt'
    if base.is_file():
        value = base.read_text(encoding='utf-8', errors='ignore').strip()
        if len(value) == 40:
            return value
    return 'UNKNOWN'

REQ='NXT3-ANNOTATION-001'

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--evidence'); ns=ap.parse_args(); root=Path(ns.root).resolve()
    checks=[]
    def c(name, ok, detail=''): checks.append({'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})
    def t(rel):
        p=root/rel; return p.read_text(encoding='utf-8') if p.is_file() else ''
    annotation=t('cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation/CpfOnlineTransaction.java')
    for token in ['requiredPermission()','auditReasonRequired()','visibility()','gatewayAllowed()','directAllowed()']:
        c('annotation-contract-'+token.split('(')[0],token in annotation)
    aspect=t('cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfOnlineTransactionAspect.java')
    bpp=t('cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfOnlineTransactionBeanPostProcessor.java')
    validator=t('cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfOnlineTransactionMetadataValidator.java')
    spi=t('cpf-starters/web/src/main/java/com/cpf/web/api/CpfOnlineTransactionPolicyEvaluator.java')
    auto=t('cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfControllerPolicyAutoConfiguration.java')
    sec=t('cpf-starters/security/resource-server/src/main/java/com/cpf/security/resource/CpfOnlineTransactionSecurityPolicyEvaluator.java')
    secauto=t('cpf-starters/security/resource-server/src/main/java/com/cpf/security/resource/CpfResourceServerAutoConfiguration.java')
    secbuild=t('cpf-starters/security/resource-server/build.gradle')
    c('runtime-aspect-consumer','@Around' in aspect and 'CpfOnlineTransaction' in aspect and 'joinPoint.proceed()' in aspect)
    c('context-required','contextSupplier.get()' in aspect and 'CPF managed transaction requires bound context' in aspect)
    c('transaction-id-preserved','CPF_TRANSACTION_ID_MUTATED' in aspect and 'transactionId.equals' in aspect)
    c('policy-evaluator-contract','interface CpfOnlineTransactionPolicyEvaluator' in spi and 'void verify' in spi)
    c('policy-missing-fail-closed','CPF_ONLINE_TX_POLICY_EVALUATOR_MISSING' in aspect)
    c('policy-ambiguous-fail-closed','CPF_ONLINE_TX_POLICY_EVALUATOR_AMBIGUOUS' in aspect)
    c('startup-metadata-validator','CpfOnlineTransactionMetadataValidator.validate' in bpp and 'putIfAbsent' in bpp)
    c('duplicate-id-fail-fast','CPF_ONLINE_TX_DUPLICATE_ID' in bpp)
    c('invalid-entry-path-fail-fast','CPF_ONLINE_TX_NO_ALLOWED_ENTRY_PATH' in validator)
    c('web-auto-config-consumer','cpfOnlineTransactionBeanPostProcessor' in auto and 'cpfOnlineTransactionAspect' in auto)
    c('resource-server-web-runtime-composition',"implementation project(':framework:web')" in secbuild)
    c('security-policy-adapter','implements CpfOnlineTransactionPolicyEvaluator' in sec)
    c('permission-exact-match','.anyMatch(required::equals)' in sec and '.contains(' not in sec)
    c('permission-authenticated','isAuthenticated()' in sec and 'CPF_ONLINE_TX_UNAUTHENTICATED' in sec)
    c('audit-reason-required','X-CPF-Audit-Reason' in sec and 'CPF_ONLINE_TX_AUDIT_REASON_REQUIRED' in sec)
    c('audit-reason-safe','MAX_AUDIT_REASON_LENGTH' in sec and 'Character::isISOControl' in sec)
    c('authorization-audit','audit.record' in sec and 'ONLINE_TRANSACTION' in sec)
    c('resource-auto-config-adapter','cpfOnlineTransactionSecurityPolicyEvaluator' in secauto)
    for dom in ['cpf-member','cpf-external']:
        texts=[p.read_text(encoding='utf-8',errors='ignore') for p in (root/dom).rglob('*.java')] if (root/dom).exists() else []
        joined='\n'.join(texts)
        c(f'{dom}-generated-consumer','@CpfTx' in joined and '@CpfRepository' in joined and '@CpfDao' not in joined and '@CpfOnlineTransaction' not in joined)
    c('web-runtime-tests',(root/'cpf-starters/web/src/test/java/com/cpf/web/runtime/CpfOnlineTransactionAspectTest.java').is_file() and (root/'cpf-starters/web/src/test/java/com/cpf/web/runtime/CpfOnlineTransactionBeanPostProcessorTest.java').is_file())
    c('security-runtime-test',(root/'cpf-starters/security/resource-server/src/test/java/com/cpf/security/resource/CpfOnlineTransactionSecurityPolicyEvaluatorTest.java').is_file())
    # Dependency-free javac smoke using current contracts plus minimal framework stubs.
    javac=shutil.which('javac')
    if not javac:
        c('javac-smoke',False,'javac unavailable')
    else:
        with tempfile.TemporaryDirectory(prefix='cpf-annotation-javac-') as td:
            td=Path(td); src=td/'src'; out=td/'out'; out.mkdir();
            def w(rel,content): p=src/rel; p.parent.mkdir(parents=True,exist_ok=True); p.write_text(content,encoding='utf-8')
            # exact current contracts copied from repository
            for rel in [
              'cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation/CpfOnlineTransaction.java',
              'cpf-starters/security/src/main/java/com/cpf/security/api/audit/CpfAuthorizationAuditEvent.java',
              'cpf-starters/security/src/main/java/com/cpf/security/api/audit/CpfAuthorizationAuditSink.java',
              'cpf-starters/web/src/main/java/com/cpf/web/api/CpfOnlineTransactionPolicyEvaluator.java',
              'cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfOnlineTransactionMetadataValidator.java',
              'cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfOnlineTransactionBeanPostProcessor.java',
              'cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfOnlineTransactionAspect.java',
              'cpf-starters/security/resource-server/src/main/java/com/cpf/security/resource/CpfOnlineTransactionSecurityPolicyEvaluator.java']:
                content=t(rel)
                if content: w('/'.join(Path(rel).parts[5 if rel.startswith('cpf-starters/base/runtime') else 6:]) if False else 'real/'+Path(rel).name,content)
            # Recreate real files at package-derived paths to avoid repository-depth assumptions.
            for rel in list((src/'real').glob('*.java')):
                content=rel.read_text(encoding='utf-8'); import re
                m=re.search(r'^package\s+([\w.]+);',content,re.M)
                pkg=m.group(1) if m else ''; dest=src/Path(*pkg.split('.'))/rel.name; dest.parent.mkdir(parents=True,exist_ok=True); dest.write_text(content,encoding='utf-8'); rel.unlink()
            # Core/context stubs
            w('com/cpf/core/api/context/CpfContext.java','package com.cpf.core.api.context; public class CpfContext { public String transactionId(){return "tx";} public String executionId(){return "ex";} public String subjectId(){return "u";} public String actorId(){return "a";} }')
            w('com/cpf/core/api/context/CpfContexts.java','package com.cpf.core.api.context; public final class CpfContexts { public static CpfContext requireCurrent(){return new CpfContext();} }')
            # AspectJ
            w('org/aspectj/lang/Signature.java','package org.aspectj.lang; public interface Signature {}')
            w('org/aspectj/lang/ProceedingJoinPoint.java','package org.aspectj.lang; public interface ProceedingJoinPoint { Object proceed() throws Throwable; Object getTarget(); Signature getSignature(); }')
            w('org/aspectj/lang/reflect/MethodSignature.java','package org.aspectj.lang.reflect; public interface MethodSignature extends org.aspectj.lang.Signature { java.lang.reflect.Method getMethod(); }')
            w('org/aspectj/lang/annotation/Aspect.java','package org.aspectj.lang.annotation; @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Aspect {}')
            w('org/aspectj/lang/annotation/Around.java','package org.aspectj.lang.annotation; @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Around { String value(); }')
            # Spring bean/core utils
            w('org/springframework/beans/BeansException.java','package org.springframework.beans; public class BeansException extends RuntimeException { public BeansException(String m){super(m);} }')
            w('org/springframework/beans/factory/config/BeanPostProcessor.java','package org.springframework.beans.factory.config; public interface BeanPostProcessor { default Object postProcessBeforeInitialization(Object bean,String name) throws org.springframework.beans.BeansException{return bean;} }')
            w('org/springframework/core/annotation/AnnotatedElementUtils.java','package org.springframework.core.annotation; public final class AnnotatedElementUtils { public static <A extends java.lang.annotation.Annotation> A findMergedAnnotation(java.lang.reflect.AnnotatedElement e,Class<A> a){return e.getAnnotation(a);} }')
            w('org/springframework/util/ClassUtils.java','package org.springframework.util; public final class ClassUtils { public static Class<?> getUserClass(Object o){return o.getClass();} }')
            w('org/springframework/util/ReflectionUtils.java','package org.springframework.util; public final class ReflectionUtils { public interface MethodCallback{void doWith(java.lang.reflect.Method m);} public static void doWithMethods(Class<?> c,MethodCallback cb){for(var m:c.getDeclaredMethods())cb.doWith(m);} }')
            # Security/web request stubs
            w('org/springframework/security/core/GrantedAuthority.java','package org.springframework.security.core; public interface GrantedAuthority { String getAuthority(); }')
            w('org/springframework/security/core/Authentication.java','package org.springframework.security.core; public interface Authentication { boolean isAuthenticated(); java.util.Collection<? extends GrantedAuthority> getAuthorities(); }')
            w('org/springframework/security/core/context/SecurityContext.java','package org.springframework.security.core.context; public interface SecurityContext { org.springframework.security.core.Authentication getAuthentication(); }')
            w('org/springframework/security/core/context/SecurityContextHolder.java','package org.springframework.security.core.context; public final class SecurityContextHolder { private static SecurityContext c=()->null; public static SecurityContext getContext(){return c;} }')
            w('jakarta/servlet/http/HttpServletRequest.java','package jakarta.servlet.http; public interface HttpServletRequest { String getHeader(String n); void setAttribute(String n,Object v); }')
            w('org/springframework/web/context/request/RequestAttributes.java','package org.springframework.web.context.request; public interface RequestAttributes {}')
            w('org/springframework/web/context/request/ServletRequestAttributes.java','package org.springframework.web.context.request; public class ServletRequestAttributes implements RequestAttributes { public jakarta.servlet.http.HttpServletRequest getRequest(){return null;} }')
            w('org/springframework/web/context/request/RequestContextHolder.java','package org.springframework.web.context.request; public final class RequestContextHolder { public static RequestAttributes getRequestAttributes(){return null;} }')
            files=[str(p) for p in src.rglob('*.java')]
            proc=subprocess.run([javac,'--release','21','-d',str(out),*files],capture_output=True,text=True)
            c('javac-smoke',proc.returncode==0,(proc.stdout+proc.stderr)[-2000:])
    fail=[x for x in checks if x['status']=='FAIL']
    result={'requirementId':REQ,'executionSourceSha':source_identity(root),'status':'PASS' if not fail else 'FAIL','failedCount':len(fail),'checks':checks,'runtimeVerification':'Static/javac PASS does not replace full Spring Boot/Java25 runtime; Gradle runtime unavailable in current environment.'}
    out=Path(ns.evidence) if ns.evidence else root/'cpf-docs/work/evidence/current/ANNOTATION_RUNTIME_CONSUMER.json'; out.parent.mkdir(parents=True,exist_ok=True); out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps({'status':result['status'],'failedCount':len(fail),'checkCount':len(checks)},ensure_ascii=False));
    if fail:
        for x in fail: print('FAIL',x['name'],x['detail'])
    raise SystemExit(1 if fail else 0)
if __name__=='__main__': main()
