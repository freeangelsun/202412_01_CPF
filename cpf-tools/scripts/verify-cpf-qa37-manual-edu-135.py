#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,subprocess,sys,tempfile
from pathlib import Path
EXPECTED={'DEV':45,'BAT':30,'ADM':17,'BZA':14,'GW':14,'OPS':15}
PRODUCT_OR_GENERATED=('cpf-admin','cpf-biz-admin','cpf-gateway','com.cpf.acc','com.cpf.mbr','com.cpf.exs')
def fail(msg): print('[CPF][QA37][EDU135][FAIL] '+msg,file=sys.stderr);raise SystemExit(1)
def main():
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--compile',action='store_true');a=p.parse_args();root=Path(a.root).resolve()
 build=root/'build.gradle'
 if not build.exists() or len(build.read_text(encoding='utf-8').splitlines())<1000:fail('root build.gradle platform contract not restored')
 for rel in ['cpf-tools/build/gradle-plugin/build.gradle','cpf-tools/build/gradle-plugin/src/main/java/com/cpf/gradle/CpfPlatformConventionPlugin.java','cpf-tools/build/platform-bom/build.gradle']:
  if not (root/rel).exists():fail('included build source missing: '+rel)
 cat=json.loads((root/'cpf-reference/src/main/resources/edu/manual-135-catalog.json').read_text(encoding='utf-8'));features=cat.get('features',[])
 if len(features)!=135:fail(f'catalog count={len(features)}')
 counts={k:0 for k in EXPECTED};ids=set();bindings=set()
 for f in features:
  rid=f['requirementId'];prefix=rid.split('-')[1];counts[prefix]+=1
  if rid in ids:fail('duplicate '+rid)
  ids.add(rid);sp=root/f['sourcePath']
  if not sp.is_file():fail('source missing '+str(sp))
  src=sp.read_text(encoding='utf-8')
  if rid not in src or f['title'] not in src:fail('source identity mismatch '+rid)
  for banned in ['TODO','FIXME','Mock success','fixed JSON','Map.of("status", "ok")']:
   if banned in src:fail(f'placeholder {banned} in {rid}')
  lowered=src.lower()
  for banned in PRODUCT_OR_GENERATED:
   if banned.lower() in lowered:fail(f'forbidden product/generated dependency {banned} in {rid}')
  if 'EduConsumerBinding consumerBinding()' not in src:fail('consumer binding missing '+rid)
  if '"cpf-reference"' not in src:fail('consumer owner must be cpf-reference '+rid)
  binding=re.search(r'new EduConsumerBinding\(\s*"([^"]+)"\s*,\s*EduConsumerType\.([A-Z_]+).*?"([^"]+)"\s*,',src,re.S)
  if not binding or binding.group(1)!=rid:fail('unparseable consumer binding '+rid)
  bindings.add((rid,binding.group(2),binding.group(3)))
  resource=f.get('resourceContract','')
  if not resource or not (root/resource).is_file():fail('resource contract missing '+rid)
  tests=f.get('tests',[])
  if len(tests)!=5:fail(f'{rid} must have five ID-specific tests, got {len(tests)}')
  for t in tests:
   if not (root/t).is_file():fail('test missing '+t)
  if f.get('implementationPackage') not in src:fail('implementation package evidence missing '+rid)
  for key in ['businessStates','exceptionScenarios','requiredVerification']:
   if not f.get(key):fail('missing '+key+' '+rid)
  if len(f['steps'])<7 or not f['failurePoints']:fail('insufficient executable contract '+rid)
 if counts!=EXPECTED:fail(f'distribution={counts}')
 if len(bindings)!=135:fail('binding count mismatch')
 ownership=root/'cpf-tools/generator/contracts/reference-edu-schema-ownership-contract.json'
 if not ownership.is_file():fail('REF schema ownership contract missing')
 for vendor in ['oracle','postgresql','mariadb']:
  for rel in [f'cpf-tools/db/vendor/{vendor}/migration/flyway/refDB/V93__manual_edu_135_operation_ledger.sql',f'cpf-tools/db/vendor/{vendor}/rollback/refDB/U93__manual_edu_135_operation_ledger.sql',f'cpf-tools/db/vendor/{vendor}/source/57_reference_edu_operation_ledger.sql',f'cpf-tools/db/vendor/{vendor}/install/01_reference_edu_operation_ledger.sql',f'cpf-tools/db/vendor/{vendor}/runtime/ref/manual_edu_135_operation_queries.sql',f'cpf-tools/db/vendor/{vendor}/verify/93_verify_manual_edu_135_operation_ledger.sql']:
   if not (root/rel).exists():fail('vendor pack missing '+rel)
  sql=(root/f'cpf-tools/db/vendor/{vendor}/migration/flyway/refDB/V93__manual_edu_135_operation_ledger.sql').read_text(encoding='utf-8').upper()
  for token in ['CPF_EDU_OPERATION','CPF_EDU_OPERATION_TARGET','CPF_EDU_OPERATION_AUDIT','CPF_EDU_OUTBOX','CPF_EDU_LEASE','CPF_EDU_BUSINESS_RECORD','CPF_EDU_COUNTERPARTY_REQUEST','IDEMPOTENCY_KEY','FENCING_TOKEN']:
   if token not in sql:fail(f'{vendor} missing token {token}')
 for pth in (root/'cpf-tools/db/vendor').glob('*/domain-template/**/*'):
  if pth.is_file() and 'CPF_EDU_' in pth.read_text(encoding='utf-8',errors='ignore').upper():fail('generated domain template contains REF EDU schema '+str(pth.relative_to(root)))
 if a.compile: compile_and_run(root)
 print(f'[CPF][QA37][EDU135][PASS] manualEdu=135 distribution={counts} bindings=135 compile={a.compile}')
def compile_and_run(root:Path):
 with tempfile.TemporaryDirectory(prefix='cpf-qa37-javac-src-') as work_name:
  compile_and_run_in(root,Path(work_name))
def compile_and_run_in(root:Path,work:Path):
 out=work/'out';out.mkdir()
 stubs={
 'org/junit/jupiter/api/Test.java':'package org.junit.jupiter.api; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface Test {}',
 'org/junit/jupiter/api/io/TempDir.java':'package org.junit.jupiter.api.io; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.FIELD,ElementType.PARAMETER}) public @interface TempDir {}',
 'org/springframework/stereotype/Component.java':'package org.springframework.stereotype; import java.lang.annotation.*; @Target(ElementType.TYPE) public @interface Component {}',
 'org/springframework/boot/autoconfigure/condition/ConditionalOnProperty.java':'package org.springframework.boot.autoconfigure.condition; import java.lang.annotation.*; @Target({ElementType.TYPE,ElementType.METHOD}) public @interface ConditionalOnProperty {String[] name() default {}; String havingValue() default ""; boolean matchIfMissing() default false;}',
 'org/junit/jupiter/api/Assertions.java':'package org.junit.jupiter.api; public final class Assertions {private Assertions(){} @FunctionalInterface public interface Executable{void execute()throws Throwable;} public static void assertTrue(boolean v){if(!v)throw new AssertionError();} public static void assertTrue(boolean v,String m){if(!v)throw new AssertionError(m);} public static void assertFalse(boolean v){if(v)throw new AssertionError();} public static void assertFalse(boolean v,String m){if(v)throw new AssertionError(m);} public static void assertEquals(Object e,Object a){if(!java.util.Objects.equals(e,a))throw new AssertionError(e+" != "+a);} public static void assertEquals(Object e,Object a,String m){if(!java.util.Objects.equals(e,a))throw new AssertionError(m);} public static void assertNotEquals(Object e,Object a){if(java.util.Objects.equals(e,a))throw new AssertionError();} public static void assertNotEquals(Object e,Object a,String m){if(java.util.Objects.equals(e,a))throw new AssertionError(m);} public static <T extends Throwable>T assertThrows(Class<T>t,Executable e){try{e.execute();}catch(Throwable x){if(t.isInstance(x))return t.cast(x);throw new AssertionError(x);}throw new AssertionError("expected "+t.getName());} public static void assertDoesNotThrow(Executable e){try{e.execute();}catch(Throwable x){throw new AssertionError(x);}}}'
 }
 stubfiles=[]
 for rel,text in stubs.items():
  p=work/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text,encoding='utf-8');stubfiles.append(str(p))
 main=root/'cpf-reference/src/main/java'
 runtime=main/'com/cpf/reference/edu/runtime'
 catalog=json.loads((root/'cpf-reference/src/main/resources/edu/manual-135-catalog.json').read_text(encoding='utf-8'))
 source_paths={root/item['sourcePath'] for item in catalog['features']}
 source_paths.update((runtime/'application').glob('*.java'))
 source_paths.update((runtime/'model').glob('*.java'))
 source_paths.update((runtime/'consumer').glob('*.java'))
 source_paths.update({runtime/'persistence/EduOperationRepository.java',runtime/'persistence/FileEduOperationRepository.java'})
 source_paths.update({
  main/'com/cpf/reference/optional/operations/config/ReferenceOperationsCapabilityContributor.java',
  main/'com/cpf/reference/optional/backoffice/config/ReferenceBackofficeCapabilityContributor.java',
  main/'com/cpf/reference/optional/gateway/config/ReferenceGatewayCapabilityContributor.java',
  main/'com/cpf/reference/batch/config/ReferenceBatchCapabilityContributor.java'})
 source=[str(path) for path in sorted(source_paths)]
 test_paths={root/test for item in catalog['features'] for test in item['tests']}
 test_paths.update((root/'cpf-reference/src/test/java/com/cpf/reference/edu/runtime').glob('*.java'))
 tests=[str(path) for path in sorted(test_paths)]
 # Windows has a short process command-line limit.  javac's documented
 # argument-file contract keeps the exact source set portable and auditable.
 javac_args=['-encoding','UTF-8','-d',str(out),*source,*tests,*stubfiles]
 argfile=work/'javac.args'
 argfile.write_text('\n'.join('"'+value.replace('\\','/').replace('"','\\"')+'"' for value in javac_args)+'\n',encoding='utf-8')
 cmd=['javac','@'+str(argfile)]
 r=subprocess.run(cmd,capture_output=True,text=True,timeout=180)
 if r.returncode:print(r.stdout);print(r.stderr,file=sys.stderr);fail('javac failed')
 r=subprocess.run(['java','-cp',str(out),'com.cpf.reference.edu.runtime.EduManual135SelfTestMain'],capture_output=True,text=True,timeout=180)
 print(r.stdout,end='')
 if r.returncode:print(r.stderr,file=sys.stderr);fail('selftest failed')
if __name__=='__main__':main()
