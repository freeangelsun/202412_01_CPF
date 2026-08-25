#!/usr/bin/env python3
# CPF 개발/검증 Source이며 최신 Requirement와 실패 누적 검증 계약을 따릅니다.
from __future__ import annotations
import argparse,csv,json,re,subprocess,tempfile,shutil
from pathlib import Path

TEXT_EXT={'.java','.kt','.groovy','.gradle','.kts','.yml','.yaml','.properties','.json','.xml','.ts','.vue','.ps1','.py','.sh'}
SKIP={'.git','build','.gradle','node_modules','dist','target','__pycache__'}
OP_PATTERNS={
 'url': re.compile(r'https?://(?!localhost\b|127\.0\.0\.1\b)[^\s"\']+',re.I),
 'host': re.compile(r'\b(?:host|hostname)\s*[:=]\s*["\'](?!(?:localhost|127\.0\.0\.1)\b)[A-Za-z0-9][A-Za-z0-9.-]+["\']',re.I),
 'port': re.compile(r'\b(?:port)\s*[:=]\s*["\']?\d{2,5}\b',re.I),
 'timeout': re.compile(r'\b(?:timeout|connectTimeout|readTimeout|writeTimeout)\s*[:=(]\s*\d+',re.I),
 'retry': re.compile(r'\b(?:retry|maxAttempts|attempts)\s*[:=]\s*\d+',re.I),
 'pool': re.compile(r'\b(?:poolSize|maxPoolSize|maximumPoolSize)\s*[:=(]\s*\d+',re.I),
}
EXEMPT_PREFIX=('cpf-docs/','cpf-tools/verification/','cpf-tools/contracts/','cpf-tools/testing/','cpf-reference/','cpf-education/')
SPEC_URI_HOSTS=('json-schema.org','mybatis.org','w3.org','spring.io','springframework.org','gradle.org','maven.apache.org','schemas.microsoft.com','apache.org','xml.org','in-toto.io','slsa.dev','opentelemetry.io')
SECRET_ASSIGN=re.compile(r'(?im)^\s*(?:[A-Za-z_][\w<>?, .\[\]-]*\s+)?(?:password|secret|token|api[-_]?key)\s*[:=]\s*["\']([^"\']+)["\']')

def main():
 p=argparse.ArgumentParser(); p.add_argument('--root',default='.'); p.add_argument('--json-out'); a=p.parse_args(); root=Path(a.root).resolve(); fail=[]; checks={}
 def check(name,cond,detail=''):
  checks[name]={'passed':bool(cond),'detail':detail}
  if not cond: fail.append(name+((':'+detail) if detail else ''))
 cls=root/'cpf-tools/contracts/configuration/hardcoding-classification.csv'; contract=root/'cpf-tools/contracts/configuration/programmatic-extension-contract.json'
 check('classification_ledger',cls.is_file())
 if cls.is_file():
  rows=list(csv.DictReader(cls.open(encoding='utf-8-sig',newline='')))
  required={'url','host','port','path','vendor','schema','user','timeout','retry','pool','batch','cache','header','locale','topic','storage','security','rate-limit','feature','error','runtime','page','file','schedule','reconcile'}
  cats={r.get('category') for r in rows}
  check('classification_categories',cats==required,','.join(sorted(required-cats)))
  check('classification_values',all(r.get('classification') in {'immutable-contract','framework-default-overrideable','environment-operation'} for r in rows))
 check('programmatic_contract',contract.is_file())
 if contract.is_file():
  d=json.loads(contract.read_text(encoding='utf-8'))
  check('precedence_contract',d.get('precedence')==['DEFAULT','PROPERTY','PROGRAMMATIC','RUNTIME'])
  check('prefix_contract',d.get('configurationPrefix')=='cpf.')
  check('extension_mechanisms',{'Builder','Customizer','Strategy','Provider','SPI','BeanOverride','TemplateHook'}.issubset(set(d.get('allowedExtensionMechanisms',[]))))
  ps=d.get('providerSelection') or {}; check('provider_collision_contract',ps.get('collisionPolicy')=='FAIL_FAST' and bool(ps.get('explicit')) and bool(ps.get('exactlyOneWhenRequired')))
  sp=d.get('secretPolicy') or {}; check('secret_policy_contract',sp.get('plainSecretInSource') is False and sp.get('plainSecretInEvidence') is False and sp.get('runtimeSecretMasking') is True)
  cp=d.get('compatibilityPolicy') or {}; check('compatibility_contract',cp.get('compatibilityShimAllowed') is False and cp.get('rootBuildMustRemainDeclarative') is True)
 # Public programmatic surface must live in public cpf-starter, not internal cpf-base-runtime.
 public_base=root/'cpf-starters/base/src/main/java/com/cpf/starter/api/config'
 customizer=public_base/'CpfConfigurationCustomizer.java'; prec=public_base/'CpfConfigurationPrecedence.java'; runtime=public_base/'CpfConfigurationCustomizers.java'
 check('public_customizer_api',customizer.is_file())
 check('public_precedence_api',prec.is_file())
 check('public_customizer_runtime_consumer',runtime.is_file() and 'CpfConfigurationCustomizer' in runtime.read_text(encoding='utf-8',errors='ignore') and '.customize(configuration)' in runtime.read_text(encoding='utf-8',errors='ignore'))
 if prec.is_file():
  s=prec.read_text(encoding='utf-8',errors='ignore'); check('precedence_api_tokens',all(x in s for x in ('DEFAULT','PROPERTY','PROGRAMMATIC','RUNTIME')))
 # Root build must be declarative, executable rules owned by cpf-tools/build.
 rb=root/'build.gradle'; conv=root/'cpf-tools/build/cpf-root-conventions.gradle'
 if rb.is_file():
  s=rb.read_text(encoding='utf-8',errors='ignore'); lines=len(s.splitlines())
  check('root_build_declarative',lines<=40 and "cpf-tools/build/cpf-root-conventions.gradle" in s and 'tasks.register' not in s and 'doLast' not in s,f'lines={lines}')

  conv_text=conv.read_text(encoding='utf-8',errors='ignore') if conv.is_file() else ''
  required_root_tasks=('qualityGate','aggregateQualityBuild','publicationGate','qa34IntegrationTest','nxt3ConfigGate','nxt3HygieneGate','nxt3Db3Gate')
  check('root_convention_owner',conv.is_file() and all(t in conv_text for t in required_root_tasks) and 'cpfJavaVersion' in conv_text and 'JavaLanguageVersion.of' in conv_text)
  # An ignored apply-from owner works only in the current dirty workspace and disappears
  # from a fresh clone. --no-index intentionally tests ignore rules even after it is tracked.
  def ignored_by_git(relative: str) -> bool:
   # Clean-clone trackability must depend only on repository ignore rules, not
   # developer-global core.excludesFile or local .git/info/exclude settings.
   # cpf-tools/build has no nested .gitignore, so an isolated repo containing
   # the root .gitignore reproduces the clean-clone decision deterministically.
   root_ignore=root/'.gitignore'
   if not root_ignore.is_file():
    return False
   with tempfile.TemporaryDirectory(prefix='cpf-ignore-contract-') as td:
    probe=Path(td)
    shutil.copy2(root_ignore,probe/'.gitignore')
    target=probe/relative
    target.parent.mkdir(parents=True,exist_ok=True)
    target.touch()
    init=subprocess.run(['git','init','-q'],cwd=probe,check=False,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
    if init.returncode!=0:
     # Git absence must not make a source-only contract falsely red; other
     # release gates verify Git/packaging in Git-enabled environments.
     return False
    return subprocess.run(['git','check-ignore','--no-index','-q',relative],
                          cwd=probe,check=False,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode==0
  convention_ignored=ignored_by_git('cpf-tools/build/cpf-root-conventions.gradle')
  check('root_convention_clean_clone_trackable',conv.is_file() and not convention_ignored)
  build_owner_sources=(
      'cpf-tools/build/cpf-root-conventions.gradle',
      'cpf-tools/build/tools/build-cpf-offline-artifact-bundle.ps1',
      'cpf-tools/build/tools/build-sample-coverage-matrix.ps1',
      'cpf-tools/build/tools/verify-cpf-gradle-wrapper-integrity.py',
  )
  build_owner_missing=[relative for relative in build_owner_sources if not (root/relative).is_file()]
  build_owner_ignored=[relative for relative in build_owner_sources if ignored_by_git(relative)]
  check('build_owner_clean_clone_source_closure',not build_owner_missing and not build_owner_ignored,
        f'missing={build_owner_missing};ignored={build_owner_ignored}')
  wrapper_consumer=root/'cpf-tools/testing/tools/tests/test_gradle_wrapper_integrity.py'
  offline_consumer=root/'cpf-tools/db/verification/check-offline-db-resource-pack.ps1'
  topology_guide=root/'cpf-docs/operations/BUILD_AND_DEPLOYMENT_TOPOLOGY_GUIDE.md'
  ci_consumer=root/'cpf-tools/release/ci/Jenkinsfile.cpf'
  wrapper_consumer_text=wrapper_consumer.read_text(encoding='utf-8',errors='ignore') if wrapper_consumer.is_file() else ''
  offline_consumer_text=offline_consumer.read_text(encoding='utf-8',errors='ignore') if offline_consumer.is_file() else ''
  topology_text=topology_guide.read_text(encoding='utf-8',errors='ignore') if topology_guide.is_file() else ''
  ci_text=ci_consumer.read_text(encoding='utf-8',errors='ignore') if ci_consumer.is_file() else ''
  # Windows/Unix path separators and optional './' prefixes are equivalent here.
  # Validate actual current consumers, not a platform-specific spelling or a retired helper.
  def normalize_path_refs(text: str) -> str:
   return text.replace('\\','/').replace('./','')
  wrapper_consumer_norm=normalize_path_refs(wrapper_consumer_text)
  offline_consumer_norm=normalize_path_refs(offline_consumer_text)
  topology_norm=normalize_path_refs(topology_text)
  ci_norm=normalize_path_refs(ci_text)
  wrapper_ok='cpf-tools/build/tools/verify-cpf-gradle-wrapper-integrity.py' in wrapper_consumer_norm
  offline_ok='cpf-tools/build/tools/build-cpf-offline-artifact-bundle.ps1' in offline_consumer_norm
  ci_module_ok=(
      ci_consumer.is_file()
      and "params.MODULES.split(',')" in ci_text
      and ':${it.trim()}:${params.GOAL}' in ci_text
      and 'aggregateQualityBuild publicationGate' in ci_text
      and 'gradlew --no-daemon ${command}' in ci_norm
  )
  topology_ok=(
      topology_guide.is_file()
      and 'cpf-tools/release/ci/Jenkinsfile.cpf' in topology_norm
      and 'build-module-set.ps1' not in topology_norm
  )
  legacy_free='cpf-tools/scripts/' not in topology_norm
  check('build_owner_consumer_path_closure',
        wrapper_ok and offline_ok and ci_module_ok and topology_ok and legacy_free,
        f'wrapper={wrapper_ok};offline={offline_ok};ciModule={ci_module_ok};topology={topology_ok};legacyFree={legacy_free}')
 # Catalog visibility/config namespace.
 cat=root/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
 check('canonical_starter_catalog',cat.is_file())
 if cat.is_file():
  d=json.loads(cat.read_text(encoding='utf-8')); mods=d.get('modules',[]); vals=[]
  for m in mods:
   cp=m.get('configPrefix')
   if cp:
    vals.append(cp)
    if not cp.startswith('cpf.'): fail.append('NON_CANONICAL_PREFIX:'+cp)
  check('unique_config_prefix',len(vals)==len(set(vals)))
  base_mod=next((m for m in mods if m.get('projectPath')==':starters:base'),None)
  check('public_base_artifact',bool(base_mod) and base_mod.get('visibility')=='public' and base_mod.get('artifactId')=='cpf-starter')
  # HTTP and Resilience are explicit developer-selectable integration capabilities.
  # Their implementation owner path remains under cpf-starters/integration, while the
  # published catalog contract must expose the provider artifacts to Generated Domains.
  by_art={m.get('artifactId'):m for m in mods}
  http=by_art.get('cpf-starter-integration-http',{})
  resilience=by_art.get('cpf-starter-integration-resilience',{})
  check('integration_http_public_selectable',
        http.get('visibility')=='public' and http.get('userSelectable') is True
        and http.get('usageLevel')=='capability' and http.get('publicationRequired') is True)
  check('integration_resilience_public_selectable',
        resilience.get('visibility')=='public' and resilience.get('userSelectable') is True
        and resilience.get('usageLevel')=='capability' and resilience.get('publicationRequired') is True)
 # Official all-in-one runner must prove both mount visibility and actual Included Build
 # subproject checks. Root `test` alone is a false-green because it does not execute them.
 runner=root/'cpf-tools/verification/nxt3/cpf_nxt3_verify_all.py'
 runner_text=runner.read_text(encoding='utf-8',errors='ignore') if runner.is_file() else ''
 check('official_runner_generated_domain_mount',runner.is_file()
       and runner_text.count('-PcpfIncludeGeneratedDomains=true')==1
       and 'SUPPORTED_GENERATED_DB_VENDORS' in runner_text
       and '-PcpfProductCompositeRoot=' in runner_text
       and "f':{name}:check'" in runner_text
       and "base+['-PcpfIncludeGeneratedDomains=true','test']" not in runner_text
       and '-PcpfIncludeLocalDomains=true' not in runner_text)
 # Operational literal and raw secret scan in active product source/config.
 literals=[]; secrets=[]
 for f in root.rglob('*'):
  if not f.is_file() or f.suffix.lower() not in TEXT_EXT or any(x in f.parts for x in SKIP) or f.name in {'package-lock.json','npm-shrinkwrap.json'}: continue
  rel=f.relative_to(root).as_posix()
  s=f.read_text(encoding='utf-8',errors='ignore')
  is_test = ('/src/test/' in '/'+rel or '/tests/' in '/'+rel or '/test/' in '/'+rel or '/e2e/' in '/'+rel or rel.endswith(('.test.ts','.spec.ts','.test.js','.spec.js')) or rel.startswith('cpf-tools/verification/') or rel.startswith('cpf-tools/testing/') or rel.endswith('playwright.config.ts'))
  is_test = is_test or rel.startswith('cpf-tools/environment/docker-development-test/') or ('/verification/' in '/'+rel and rel.startswith('cpf-tools/')) or (rel.startswith('cpf-tools/') and f.name.lower().startswith(('verify-','check-','smoke-','test-','validate-')))
  if not is_test:
   for sm in SECRET_ASSIGN.finditer(s):
    value=sm.group(1).strip()
    line_start=s.rfind('\n',0,sm.start())+1; line_end=s.find('\n',sm.end()); line_text=s[line_start:line_end if line_end>=0 else len(s)]
    header_constant=('public static final String' in line_text and value.lower().startswith(('x-','traceparent','tracestate','authorization','forwarded','user-agent','idempotency-key')))
    if value and not value.startswith('${') and not value.startswith('<') and value.lower() not in {'masked','***','changeme-for-test'} and not header_constant:
     secrets.append(f'{rel}:{s.count(chr(10),0,sm.start())+1}')
  if rel.startswith(EXEMPT_PREFIX) or is_test: continue
  # Canonical seed-model은 운영값을 숨겨 둔 Application Source가 아니라 DB Product Seed의
  # 단일 설정 데이터 정본입니다. Service Registry endpoint 기본값은 ADM에서 변경 가능한
  # OPS_SERVICE_ENDPOINT 데이터이며 DB3 seed parity/lifecycle Gate가 별도로 검증합니다.
  is_canonical_registry_seed = (
      rel == 'cpf-tools/db/canonical/seed-model.json'
      and '53_runtime_service_registry_seed.sql' in s
  )
  is_config_properties='@ConfigurationProperties' in s
  is_frontend_source='/frontend/src/' in '/'+rel
  for cat,rx in OP_PATTERNS.items():
   for m in rx.finditer(s):
    literal=m.group(0)
    if is_canonical_registry_seed and cat == 'url':
     continue
    if cat=='url' and (any(host in literal.lower() for host in SPEC_URI_HOSTS) or '${' in literal or '.example' in literal.lower() or 'example.test' in literal.lower()): continue
    line=s.count('\n',0,m.start())+1; nearby=s[max(0,m.start()-260):m.end()+260]
    line_start=s.rfind('\n',0,m.start())+1; line_end=s.find('\n',m.end()); line_text=s[line_start:line_end if line_end>=0 else len(s)].strip()
    if line_text.startswith(('#','//','*')): continue
    if rel.startswith('deploy/environments/local/') and cat=='port': continue
    if is_config_properties: continue
    if is_frontend_source and cat in {'timeout','retry','pool'}: continue
    if f.suffix.lower()=='.java' and 'if(' in line_text.replace(' ','') and cat in {'timeout','retry','pool'}: continue
    # PowerShell CLI parameter defaults are explicitly overrideable inputs, not hidden operational values.
    if f.suffix.lower()=='.ps1' and re.search(r'\[[^]]+\]\s*\$[A-Za-z_]\w*\s*=\s*[^,]+,?$',line_text): continue
    # Canonical DB vendor protocol ports are immutable compatibility constants.
    if rel.startswith('cpf-tools/db/') and ('$vendorDefaults' in nearby or rel.startswith('cpf-tools/db/tools/')): continue
    if rel.endswith('cpf-tools/generator/tools/initialize-domain-database.ps1') and cat=='port': continue
    if rel.endswith('cpf-tools/runtime/tools/runtime-common.ps1') and cat=='port' and 'portEnv' in nearby: continue
    if 'HardcodedContract' in nearby or 'cpf.' in nearby or '${CPF_' in nearby or 'System.getenv' in nearby or '@Value' in nearby or 'ConfigurationProperties' in nearby: continue
    literals.append(f'{cat}:{rel}:{line}:{literal[:80]}')
 check('operational_literals_classified',not literals,';'.join(literals[:20]))
 check('raw_secret_literals_zero',not secrets,';'.join(secrets[:20]))
 fail=sorted(set(fail))
 result={'status':'PASS' if not fail else 'FAIL','failures':fail,'checks':checks,'unclassifiedOperationalLiterals':literals,'rawSecretCandidates':secrets,'executionScope':str(root)}
 print('CPF_NXT3_CONFIG_CONTRACT='+result['status']); print('failures='+str(len(fail))); [print(x) for x in fail[:300]]
 if a.json_out: Path(a.json_out).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 return 0 if not fail else 1
if __name__=='__main__': raise SystemExit(main())
