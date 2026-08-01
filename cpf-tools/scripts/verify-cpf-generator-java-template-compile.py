#!/usr/bin/env python3
from pathlib import Path
import re, shutil, subprocess, sys, tempfile
root=Path(sys.argv[1] if len(sys.argv)>1 else '.').resolve()
src=(root/'cpf-tools/scripts/create-domain.ps1').read_text(encoding='utf-8')
tmp=tempfile.TemporaryDirectory(prefix='cpf-generator-compile-')
out=Path(tmp.name)
(out/'src').mkdir(parents=True)
vals={'${FeatureClassPrefix}':'Sample','${ModuleClassName}':'Sample','$FeaturePackage':'com.example.sample.sampleitem','$BasePackage':'com.example.sample','$ModuleClassName':'Sample','$module':'sample','$ModuleUpper':'SMP'}
def block(name):
 m=re.search(rf'\${name} = @"\n(.*?)\n"@',src,re.S)
 if not m: raise Exception(name)
 t=m.group(1)
 for a,b in vals.items(): t=t.replace(a,b)
 t=re.sub(r"\$\(\[string\]::Concat\('import ', com\.example\.sample, '\.common\.base\.', Sample, 'BaseService;'\)\)",'import com.example.sample.common.base.SampleBaseService;',t)
 t=re.sub(r"\$\(\[string\]::Concat\('import ', com\.example\.sample, '\.common\.contract\.', Sample, '(Request|Response);'\)\)",lambda m:'import com.example.sample.common.contract.Sample'+m.group(1)+';',t)
 return t
blocks=['commandPortSource','inMemoryAdapter','service','repository','sampleCommand','sampleItem','searchResult','deleteCommand','deleteResult','idempotencyEntry']
for name in blocks:
 t=block(name)
 m=re.search(r'package\s+([\w.]+);',t); c=re.search(r'public\s+(?:class|interface|record)\s+(\w+)',t)
 if not (m and c): raise Exception((name,t[:200]))
 p=out/'src'/Path(*m.group(1).split('.'))/(c.group(1)+'.java');p.parent.mkdir(parents=True,exist_ok=True);p.write_text(t, encoding="utf-8")
# required DTO/search request and ports/stubs
extra={
'com/example/sample/sampleitem/dto/SampleSearchRequest.java':'''package com.example.sample.sampleitem.dto; public record SampleSearchRequest(String keyword,Integer page,Integer size,String sortBy,String sortDirection){ public SampleSearchRequest normalized(){return this;} }''',
'com/example/sample/sampleitem/port/SampleQueryPort.java':'''package com.example.sample.sampleitem.port; import com.example.sample.sampleitem.dto.*; import com.cpf.core.api.page.CpfSlice; import java.util.Optional; public interface SampleQueryPort { SampleSearchResult search(SampleSearchRequest r); Optional<SampleSampleItem> findBySampleKey(String k); CpfSlice<SampleSampleItem> cursor(Long a,int s); }''',
'com/example/sample/common/base/SampleBaseService.java':'package com.example.sample.common.base; public abstract class SampleBaseService {}',
'com/example/sample/common/contract/SampleRequest.java':'package com.example.sample.common.contract; public interface SampleRequest {}',
'com/example/sample/common/contract/SampleResponse.java':'package com.example.sample.common.contract; public interface SampleResponse {}',
'com/cpf/core/api/page/CpfSlice.java':'package com.cpf.core.api.page; import java.util.List; public record CpfSlice<T>(List<T> items,int page,int size,boolean hasNext) {}',
'com/cpf/core/api/error/CpfValidationException.java':'package com.cpf.core.api.error; public class CpfValidationException extends RuntimeException { public CpfValidationException(String m){super(m);} }',
'com/cpf/core/api/logging/CpfTransactionContext.java':'''package com.cpf.core.api.logging; public final class CpfTransactionContext { public static String idempotencyKey(){return "I";} public static String operatorId(){return "O";} public static String userId(){return "U";} public static String transactionId(){return "T";} public static long nextSequence(){return 1;} }''',
'com/cpf/core/api/security/CpfMasking.java':'package com.cpf.core.api.security; public final class CpfMasking { public static String mask(String s){return s;} }',
'org/springframework/stereotype/Service.java':'package org.springframework.stereotype; public @interface Service {}',
'org/springframework/stereotype/Component.java':'package org.springframework.stereotype; public @interface Component {}',
'org/springframework/stereotype/Repository.java':'package org.springframework.stereotype; public @interface Repository {}',
'org/springframework/context/annotation/Profile.java':'package org.springframework.context.annotation; public @interface Profile { String value(); }',
'org/springframework/transaction/annotation/Transactional.java':'package org.springframework.transaction.annotation; public @interface Transactional { boolean readOnly() default false; }',
'org/springframework/dao/OptimisticLockingFailureException.java':'package org.springframework.dao; public class OptimisticLockingFailureException extends RuntimeException { public OptimisticLockingFailureException(String m){super(m);} }',
'org/springframework/beans/factory/annotation/Qualifier.java':'package org.springframework.beans.factory.annotation; public @interface Qualifier { String value(); }',
'org/springframework/transaction/PlatformTransactionManager.java':'package org.springframework.transaction; public interface PlatformTransactionManager {}',
'org/springframework/transaction/support/TransactionTemplate.java':'''package org.springframework.transaction.support; import org.springframework.transaction.PlatformTransactionManager; import java.util.function.Consumer; public class TransactionTemplate { public TransactionTemplate(PlatformTransactionManager m){} public void executeWithoutResult(Consumer<Status> c){c.accept(new Status());} public static class Status { public void setRollbackOnly(){} } }''',
'org/mybatis/spring/SqlSessionTemplate.java':'''package org.mybatis.spring; import java.util.*; public class SqlSessionTemplate { public <T> T selectOne(String s,Object p){return null;} public <E> List<E> selectList(String s,Object p){return List.of();} public int insert(String s,Object p){return 1;} public int update(String s,Object p){return 1;} }''',
'jakarta/validation/constraints/NotBlank.java':'package jakarta.validation.constraints; public @interface NotBlank {}',
'jakarta/validation/constraints/Pattern.java':'package jakarta.validation.constraints; public @interface Pattern { String regexp(); }',
'jakarta/validation/constraints/PositiveOrZero.java':'package jakarta.validation.constraints; public @interface PositiveOrZero {}',
'jakarta/validation/constraints/Size.java':'package jakarta.validation.constraints; public @interface Size { int max(); }',
}
for rel,t in extra.items(): p=out/'src'/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(t, encoding="utf-8")
files=[str(x) for x in (out/'src').rglob('*.java')]
proc=subprocess.run(['javac','--release','21','-d',str(out/'classes'),*files],capture_output=True,text=True)
if proc.stdout.strip(): print(proc.stdout.strip())
if proc.stderr.strip(): print(proc.stderr.strip())
if proc.returncode==0:
 print(f'GENERATOR_JAVA_TEMPLATE_COMPILE=PASS sources={len(files)} release=21')
else:
 print(f'GENERATOR_JAVA_TEMPLATE_COMPILE=FAIL sources={len(files)} release=21')
raise SystemExit(proc.returncode)
