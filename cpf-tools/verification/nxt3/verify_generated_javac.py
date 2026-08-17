#!/usr/bin/env python3
# CPF 개발/검증 Source이며 최신 Requirement와 실패 누적 검증 계약을 따릅니다.
from __future__ import annotations
import argparse,json,shutil,subprocess,tempfile
from pathlib import Path

ACTUAL_PRODUCT_SOURCE_DIRS=(
    'cpf-core/src/main/java/com/cpf/core/api/base',
    'cpf-core/src/main/java/com/cpf/core/api/result',
    'cpf-core/src/main/java/com/cpf/core/api/domain',
    'cpf-starters/integration/src/main/java/com/cpf/integration/api/domaincall',
)

STUBS={
'com/cpf/core/api/context/CpfContexts.java':'''package com.cpf.core.api.context; public final class CpfContexts { public static CpfContext current(){return new CpfContext();} public static CpfContext requireCurrent(){return new CpfContext();} public static AutoCloseable bind(CpfContextSnapshot snapshot){return () -> {};} public static long transactionSequence(){return 1L;} public static String operatorId(){return "OPERATOR";} public static final class CpfContext { public String transactionId(){return "TX";} public CpfContext localDomainHop(String systemCode,String operationId){return this;} } }''',
'com/cpf/core/api/context/CpfContextSnapshot.java':'''package com.cpf.core.api.context; public final class CpfContextSnapshot { public static CpfContextSnapshot capture(CpfContexts.CpfContext context){return new CpfContextSnapshot();} }''',
'com/cpf/foundation/execution/api/CpfOnlineTransaction.java':'''package com.cpf.foundation.execution.api; import java.lang.annotation.*; @Target({ElementType.METHOD,ElementType.TYPE}) @Retention(RetentionPolicy.RUNTIME) public @interface CpfOnlineTransaction { String operationId(); String name(); String description(); }''',
'com/cpf/core/api/error/CpfValidationException.java':'''package com.cpf.core.api.error; public final class CpfValidationException extends RuntimeException { public CpfValidationException(String s){super(s);} }''',
'com/cpf/core/api/error/CpfErrorCode.java':'''package com.cpf.core.api.error; public enum CpfErrorCode { DATABASE_ERROR, CONFLICT, DUPLICATE, NOT_FOUND, BUSINESS_RULE_VIOLATION }''',
'com/cpf/core/api/error/CpfBusinessException.java':'''package com.cpf.core.api.error; public final class CpfBusinessException extends RuntimeException { public CpfBusinessException(CpfErrorCode c,String s){super(s);} }''',
'com/cpf/foundation/api/CpfBaseService.java':'''package com.cpf.foundation.api; public abstract class CpfBaseService { protected final String requireText(String v,String n){if(v==null||v.isBlank()) throw new IllegalArgumentException(n); return v.trim();} }''',
'com/cpf/data/persistence/api/CpfBaseRepository.java':'''package com.cpf.data.persistence.api; public abstract class CpfBaseRepository { protected final int boundedSize(int r,int d,int m){return r<=0?d:Math.min(r,m);} protected final void requireRule(boolean c,String m){if(!c)throw new IllegalArgumentException(m);} protected final String requireText(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n);return v.trim();} }''',
'com/cpf/data/persistence/api/CpfRepository.java':'''package com.cpf.data.persistence.api; import java.lang.annotation.*; @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) public @interface CpfRepository {}''',
'com/cpf/web/api/CpfBaseController.java':'''package com.cpf.web.api; import java.net.URI; import org.springframework.http.ResponseEntity; public abstract class CpfBaseController { protected final void requireRule(boolean c,String m){if(!c)throw new IllegalArgumentException(m);} protected final <T> ResponseEntity<T> ok(T b){return ResponseEntity.ok(b);} protected final <T> ResponseEntity<T> created(URI u,T b){return ResponseEntity.created(u).body(b);} }''',
'com/cpf/web/api/CpfRestController.java':'''package com.cpf.web.api; import java.lang.annotation.*; @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) public @interface CpfRestController { String value() default ""; }''',
'com/cpf/foundation/annotation/CpfService.java':'''package com.cpf.foundation.annotation; import java.lang.annotation.*; @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) public @interface CpfService {}''',
'com/cpf/data/persistence/api/annotation/CpfTransactional.java':'''package com.cpf.data.persistence.api.annotation; import java.lang.annotation.*; @Target({ElementType.METHOD,ElementType.TYPE}) @Retention(RetentionPolicy.RUNTIME) public @interface CpfTransactional { boolean readOnly() default false; }''',
'com/cpf/batch/api/annotation/CpfBatchJob.java':'''package com.cpf.batch.api.annotation; import java.lang.annotation.*; @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) public @interface CpfBatchJob { String value(); boolean restartable() default true; int maxConcurrentExecutions() default 1; }''',
'com/cpf/batch/api/annotation/CpfBatchStep.java':'''package com.cpf.batch.api.annotation; import java.lang.annotation.*; @Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) public @interface CpfBatchStep { String value(); int order() default 0; boolean idempotent() default true; }''',
'com/cpf/batch/spi/BatchStepHandler.java':'''package com.cpf.batch.spi; import java.util.Map; public interface BatchStepHandler { record BatchStepResult(Status status,String code,String message,long readCount,long writeCount,long skipCount,Map<String,Object> checkpoint) { public static BatchStepResult completed(String message,long read,long write,Map<String,Object> checkpoint){return new BatchStepResult(Status.COMPLETED,"",message,read,write,0,checkpoint);} } enum Status { COMPLETED,FAILED,RETRYABLE_FAILURE,UNKNOWN_RESULT,STOPPED } }''',
'org/apache/ibatis/annotations/Mapper.java':'''package org.apache.ibatis.annotations; import java.lang.annotation.*; @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) public @interface Mapper {}''',
'org/apache/ibatis/annotations/Param.java':'''package org.apache.ibatis.annotations; import java.lang.annotation.*; @Target(ElementType.PARAMETER) @Retention(RetentionPolicy.RUNTIME) public @interface Param { String value(); }''',
'org/apache/ibatis/session/RowBounds.java':'''package org.apache.ibatis.session; public final class RowBounds { public RowBounds(int offset,int limit){} }''',
'org/mybatis/spring/annotation/MapperScan.java':'''package org.mybatis.spring.annotation; import java.lang.annotation.*; @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) public @interface MapperScan { String[] value() default {}; }''',
'org/springframework/stereotype/Component.java':'''package org.springframework.stereotype; import java.lang.annotation.*; @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) public @interface Component {}''',
'org/springframework/dao/DuplicateKeyException.java':'''package org.springframework.dao; public class DuplicateKeyException extends RuntimeException { public DuplicateKeyException(String message){super(message);} }''',
'org/springframework/transaction/annotation/Transactional.java':'''package org.springframework.transaction.annotation; import java.lang.annotation.*; @Target({ElementType.TYPE,ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME) public @interface Transactional { boolean readOnly() default false; }''',
'org/springframework/boot/SpringApplication.java':'''package org.springframework.boot; public final class SpringApplication { public static Object run(Class<?> c,String[] a){return null;} }''',
'org/springframework/boot/autoconfigure/SpringBootApplication.java':'''package org.springframework.boot.autoconfigure; import java.lang.annotation.*; @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) public @interface SpringBootApplication { String[] scanBasePackages() default {}; }''',
'org/springframework/http/ResponseEntity.java':'''package org.springframework.http; import java.net.URI; public class ResponseEntity<T> { public static <T> ResponseEntity<T> ok(T b){return new ResponseEntity<>();} public static BodyBuilder created(URI u){return new BodyBuilder();} public static class BodyBuilder { public <T> ResponseEntity<T> body(T b){return new ResponseEntity<>();} } }''',
'org/springframework/web/bind/annotation/RequestMapping.java':'''package org.springframework.web.bind.annotation; import java.lang.annotation.*; @Target({ElementType.TYPE,ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME) public @interface RequestMapping { String[] value() default {}; }''',
'org/springframework/web/bind/annotation/PostMapping.java':'''package org.springframework.web.bind.annotation; import java.lang.annotation.*; @Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) public @interface PostMapping { String[] value() default {}; }''',
'org/springframework/web/bind/annotation/GetMapping.java':'''package org.springframework.web.bind.annotation; import java.lang.annotation.*; @Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) public @interface GetMapping { String[] value() default {}; }''',
'org/springframework/web/bind/annotation/PutMapping.java':'''package org.springframework.web.bind.annotation; import java.lang.annotation.*; @Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) public @interface PutMapping { String[] value() default {}; }''',
'org/springframework/web/bind/annotation/DeleteMapping.java':'''package org.springframework.web.bind.annotation; import java.lang.annotation.*; @Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) public @interface DeleteMapping { String[] value() default {}; }''',
'org/springframework/web/bind/annotation/PatchMapping.java':'''package org.springframework.web.bind.annotation; import java.lang.annotation.*; @Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) public @interface PatchMapping { String[] value() default {}; }''',
'org/springframework/web/bind/annotation/PathVariable.java':'''package org.springframework.web.bind.annotation; import java.lang.annotation.*; @Target(ElementType.PARAMETER) @Retention(RetentionPolicy.RUNTIME) public @interface PathVariable { String value() default ""; }''',
'org/springframework/web/bind/annotation/RequestBody.java':'''package org.springframework.web.bind.annotation; import java.lang.annotation.*; @Target(ElementType.PARAMETER) @Retention(RetentionPolicy.RUNTIME) public @interface RequestBody {}''',
'org/springframework/web/bind/annotation/RequestParam.java':'''package org.springframework.web.bind.annotation; import java.lang.annotation.*; @Target(ElementType.PARAMETER) @Retention(RetentionPolicy.RUNTIME) public @interface RequestParam { String value() default ""; boolean required() default true; String defaultValue() default ""; }''',
'jakarta/validation/Valid.java':'''package jakarta.validation; import java.lang.annotation.*; @Target({ElementType.PARAMETER,ElementType.FIELD}) @Retention(RetentionPolicy.RUNTIME) public @interface Valid {}''',
'jakarta/validation/constraints/NotBlank.java':'''package jakarta.validation.constraints; import java.lang.annotation.*; @Target({ElementType.RECORD_COMPONENT,ElementType.FIELD,ElementType.PARAMETER}) @Retention(RetentionPolicy.RUNTIME) public @interface NotBlank {}''',
'jakarta/validation/constraints/Size.java':'''package jakarta.validation.constraints; import java.lang.annotation.*; @Target({ElementType.RECORD_COMPONENT,ElementType.FIELD,ElementType.PARAMETER}) @Retention(RetentionPolicy.RUNTIME) public @interface Size { int max() default Integer.MAX_VALUE; int min() default 0; }''',
'jakarta/validation/constraints/Min.java':'''package jakarta.validation.constraints; import java.lang.annotation.*; @Target({ElementType.RECORD_COMPONENT,ElementType.FIELD,ElementType.PARAMETER}) @Retention(RetentionPolicy.RUNTIME) public @interface Min { long value(); }''',
'io/swagger/v3/oas/annotations/Operation.java':'''package io.swagger.v3.oas.annotations; import java.lang.annotation.*; @Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) public @interface Operation { String operationId() default ""; String summary() default ""; }''',
'org/slf4j/Logger.java':'''package org.slf4j; public interface Logger { void info(String f,Object... a); }''',
'org/slf4j/LoggerFactory.java':'''package org.slf4j; public final class LoggerFactory { private static final Logger L=(f,a)->{}; public static Logger getLogger(Class<?> c){return L;} }''',
'org/junit/jupiter/api/Test.java':'''package org.junit.jupiter.api; import java.lang.annotation.*; @Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) public @interface Test {}''',
'org/assertj/core/api/Assertions.java':'''package org.assertj.core.api; public final class Assertions { public interface ThrowingCallable { void call() throws Throwable; } public static <T> GenericAssert<T> assertThat(T v){return new GenericAssert<>();} public static GenericAssert<Throwable> assertThatThrownBy(ThrowingCallable c){try{c.call();}catch(Throwable t){} return new GenericAssert<>();} public static final class GenericAssert<T>{ public GenericAssert<T> isEqualTo(Object o){return this;} public GenericAssert<T> isNotNull(){return this;} public GenericAssert<T> isTrue(){return this;} public GenericAssert<T> isInstanceOf(Class<?> c){return this;} } }''',
}

def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root',type=Path,required=True)
    ap.add_argument('--evidence',type=Path)
    ap.add_argument(
        '--domain-root',
        action='append',
        type=Path,
        help='컴파일할 Generated Domain root입니다. 반복 지정 가능하며 생략하면 cpf-member/cpf-external을 검증합니다.',
    )
    ns=ap.parse_args(); root=ns.root.resolve()
    domain_roots=[
        (candidate if candidate.is_absolute() else root/candidate).resolve()
        for candidate in (ns.domain_root or [Path('cpf-member'),Path('cpf-external')])
    ]
    javac=shutil.which('javac'); checks=[]
    if not javac:
        result={'gate':'NXT3_GENERATED_JAVAC','status':'UNVERIFIED','reason':'javac unavailable'}
    else:
        with tempfile.TemporaryDirectory(prefix='cpf-generated-javac-') as td:
            t=Path(td); stub=t/'stubs'; classes=t/'classes'
            for rel,src in STUBS.items():
                p=stub/rel; p.parent.mkdir(parents=True,exist_ok=True); p.write_text(src+'\n',encoding='utf-8')
            sources=[str(p) for p in stub.rglob('*.java')]
            # Generated Domain의 핵심 Domain Call 계약은 stub이 아니라 실제 CPF Product Source와 함께 컴파일해 API drift를 검출합니다.
            for relative in ACTUAL_PRODUCT_SOURCE_DIRS:
                sources.extend(str(p) for p in (root/relative).glob('*.java'))
            for domain_root in domain_roots:
                domain=domain_root.name
                if not domain.startswith('cpf-') or not domain_root.is_dir():
                    checks.append({'domain':domain,'status':'FAIL','rc':2,'stderr':f'Generated Domain root가 유효하지 않습니다: {domain_root}','sourceCount':0})
                    continue
                dsrc=[str(p) for p in domain_root.rglob('*.java')]
                if not dsrc:
                    checks.append({'domain':domain,'status':'FAIL','rc':2,'stderr':f'Generated Java Source가 없습니다: {domain_root}','sourceCount':0})
                    continue
                cp=subprocess.run([javac,'--release','21','-encoding','UTF-8','-d',str(classes),*sources,*dsrc],text=True,capture_output=True)
                checks.append({'domain':domain,'status':'PASS' if cp.returncode==0 else 'FAIL','rc':cp.returncode,'stderr':cp.stderr[-12000:],'sourceCount':len(dsrc)})
            failed=[x for x in checks if x['status']=='FAIL']; result={'gate':'NXT3_GENERATED_JAVAC','status':'PASS' if not failed else 'FAIL','javaRelease':21,'checks':checks,'failedCount':len(failed)}
    if ns.evidence:
        ev=ns.evidence if ns.evidence.is_absolute() else root/ns.evidence; ev.parent.mkdir(parents=True,exist_ok=True); ev.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 1 if result.get('status')=='FAIL' else 0
if __name__=='__main__': raise SystemExit(main())
