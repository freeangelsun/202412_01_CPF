#!/usr/bin/env python3
from __future__ import annotations
from pathlib import Path
import csv, hashlib, shutil, subprocess, tempfile, textwrap

ROOT=Path(__file__).resolve().parents[2]
FIELDS=[
'exception_id','module','capability','artifact','version','owner','reason','standard_path_gap','environments','security_impact','license_review','supply_chain_review','operations_responsibility','approved_by','approved_at','expires_at','rollback','return_plan','rule_ids','config_files','evidence_path','status','config_hash']
HASH_FIELDS=FIELDS[:-1]
STANDARDS='standard-error,header-context,transaction-id,security-boundary,audit,masking,observability,config,dependency-version,architecture-gate'
NOW='2026-08-09T00:00:00Z'

def sha(b:bytes)->str:return hashlib.sha256(b).hexdigest()
def config_hash(row:dict[str,str], config:bytes)->str:
    canonical='\x1f'.join(row[k].strip().replace('\r\n','\n').replace('\r','\n') for k in HASH_FIELDS).encode()
    path=row['config_files'].replace('\\','/').encode()
    return sha(canonical+b'\x00'+path+b'\x00'+config+b'\x00')
def csv_bytes(row:dict[str,str])->bytes:
    import io
    s=io.StringIO(newline='')
    w=csv.DictWriter(s,fieldnames=FIELDS,lineterminator='\n')
    w.writeheader();w.writerow(row)
    return s.getvalue().encode()
def base_row()->dict[str,str]:
    return dict(
      exception_id='CPF-EX-DEMO',module='demo-domain',capability='integration',
      artifact='com.example:demo-provider',version='1.2.3',owner='demo-team',
      reason='temporary provider bridge',standard_path_gap='provider not yet standardized',
      environments='dev;stg',security_impact='reviewed',license_review='approved',
      supply_chain_review='approved',operations_responsibility='demo-team',approved_by='qa-owner',
      approved_at='2026-08-01T00:00:00Z',expires_at='2026-12-31T00:00:00Z',
      rollback='remove provider dependency',return_plan='migrate to canonical provider',rule_ids='CPF-RULE-001',
      config_files='src/main/resources/config/demo.yml',evidence_path='evidence/demo.md',status='APPROVED',config_hash='')

def write_fixture(root:Path, scenario:str)->tuple[str,str,str]:
    config=b'demo:\n  enabled: true\n'
    row=base_row()
    expected='PASS'
    fragment=''
    if scenario=='UNAPPROVED': row['status']='PENDING'; expected='FAIL'; fragment='not approved'
    elif scenario=='EXPIRED': row['expires_at']='2026-08-01T00:00:00Z'; expected='FAIL'; fragment='expired'
    row['config_hash']=config_hash(row,config)
    registry=csv_bytes(row)
    registry_hash=sha(registry)
    locked_ids='CPF-EX-DEMO'
    if scenario=='UNREGISTERED': locked_ids=''; expected='FAIL'; fragment='Approved exception id drift'
    if scenario=='REGISTRY_HASH_DRIFT': registry_hash='0'*64; expected='FAIL'; fragment='exceptionRegistrySha256 mismatch'
    (root/'META-INF/cpf').mkdir(parents=True)
    (root/'config').mkdir(parents=True)
    (root/'META-INF/cpf/cpf-approved-exceptions.csv').write_bytes(registry)
    (root/'config/demo.yml').write_bytes(config)
    policy='\n'.join([
      'policyVersion=1.0','failClosed=true','module=demo-domain','profile=base','capabilities=integration',
      f'requiredStandards={STANDARDS}',f'exceptionRegistrySha256={registry_hash}',f'approvedExceptionIds={locked_ids}',''])
    (root/'META-INF/cpf/generated-domain-policy.properties').write_text(policy,encoding='utf-8')
    active_hash=row['config_hash']
    active_version=row['version']
    if scenario=='CONFIG_HASH_DRIFT': active_hash='f'*64; expected='FAIL'; fragment='active_config_hash'
    if scenario=='VERSION_DRIFT': active_version='9.9.9'; expected='FAIL'; fragment='active_artifact_version'
    props=';'.join([f'cpf.generated-domain.environment=dev',f'cpf.generated-domain.approved-exceptions.CPF-EX-DEMO.config-hash={active_hash}',f'cpf.generated-domain.approved-exceptions.CPF-EX-DEMO.artifact-version={active_version}'])
    return expected,fragment,props

def main()->int:
    tmp=Path(tempfile.mkdtemp(prefix='cpf-domain-ex-'))
    try:
        classes=tmp/'classes'; classes.mkdir()
        harness=tmp/'CpfDomainExceptionRuntimeHarness.java'
        harness.write_text(textwrap.dedent('''
          import com.cpf.starter.runtime.CpfGeneratedDomainPolicyRuntimeVerifier;
          import java.net.URLClassLoader;
          import java.nio.file.Path;
          import java.time.Clock;
          import java.time.Instant;
          import java.time.ZoneOffset;
          import java.util.HashMap;
          import java.util.Map;
          public final class CpfDomainExceptionRuntimeHarness {
            public static void main(String[] args) throws Exception {
              Path root=Path.of(args[0]); String expected=args[1]; String fragment=args[2];
              Map<String,String> props=new HashMap<>();
              if(args.length>3&&!args[3].isBlank()) for(String p:args[3].split(";",-1)){int i=p.indexOf('='); if(i>0)props.put(p.substring(0,i),p.substring(i+1));}
              try(URLClassLoader loader=new URLClassLoader(new java.net.URL[]{root.toUri().toURL()}, CpfDomainExceptionRuntimeHarness.class.getClassLoader())){
                var verifier=new CpfGeneratedDomainPolicyRuntimeVerifier(loader,Clock.fixed(Instant.parse("2026-08-09T00:00:00Z"),ZoneOffset.UTC),props::get);
                try { var r=verifier.verify(); if(!"PASS".equals(expected))throw new IllegalStateException("expected failure but got "+r); }
                catch(IllegalStateException ex){ if("PASS".equals(expected))throw ex; if(fragment!=null&&!fragment.isBlank()&&!ex.getMessage().contains(fragment))throw new IllegalStateException("wrong failure: "+ex.getMessage(),ex); return; }
              }
            }
          }
        '''),encoding='utf-8')
        src=ROOT/'cpf-starters/base/runtime/src/main/java/com/cpf/starter/runtime/CpfGeneratedDomainPolicyRuntimeVerifier.java'
        subprocess.run(['javac','-encoding','UTF-8','-d',str(classes),str(src),str(harness)],check=True)
        scenarios=['PASS','UNREGISTERED','UNAPPROVED','EXPIRED','VERSION_DRIFT','REGISTRY_HASH_DRIFT','CONFIG_HASH_DRIFT']
        for scenario in scenarios:
            fx=tmp/scenario; fx.mkdir()
            expected,fragment,props=write_fixture(fx,scenario)
            subprocess.run(['java','-cp',str(classes),'CpfDomainExceptionRuntimeHarness',str(fx),expected,fragment,props],check=True)
            print(f'CPF_DOMAIN_EXCEPTION_RUNTIME_{scenario}=PASS')
        print('CPF_DOMAIN_EXCEPTION_RUNTIME=PASS cases=7')
        return 0
    finally: shutil.rmtree(tmp,ignore_errors=True)
if __name__=='__main__': raise SystemExit(main())
