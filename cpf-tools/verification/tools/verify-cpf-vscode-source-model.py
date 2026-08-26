#!/usr/bin/env python3
"""Source-controlled VS Code/Buildship model checks; Fresh IDE synchronization remains a runtime step."""
from pathlib import Path
import argparse,json,re
try:
 import yaml
except Exception:
 yaml=None

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();root=Path(a.root).resolve();errors=[]
 def text(rel): return (root/rel).read_text(encoding='utf-8')
 # Break the real Gradle/JDT project cycle: testkit may use resilience; resilience tests must not depend on testkit.
 rb=text('cpf-starters/integration/resilience/build.gradle')
 if "project(':internal:testing:testkit')" in rb: errors.append('resilience:test->cpf-testkit cycle remains')
 if 'com.cpf.testkit' in '\n'.join(p.read_text(encoding='utf-8') for p in (root/'cpf-starters/integration/resilience/src/test').rglob('*.java')): errors.append('resilience tests still import cpf-testkit')
 cb=text('cpf-starters/common/build.gradle')
 if "java.setSrcDirs(['src/test/java/com/cpf/common/runtime'])" in cb: errors.append('starter-common nested test source root breaks Java package model')
 if "java.setSrcDirs(['src/test/java'])" not in cb: errors.append('starter-common canonical test source root missing')
 # HTTP security types must have a declared compile/test classpath owner.
 hb=text('cpf-starters/integration/http/build.gradle')
 for dep in ["compileOnly 'org.springframework.boot:spring-boot-starter-security'","testImplementation 'org.springframework.boot:spring-boot-starter-security'","testImplementation 'org.springframework.security:spring-security-test'"]:
  if dep not in hb: errors.append('integration-http missing dependency '+dep)
 # Batch source owners must declare center-cut/testkit dependencies rather than relying on IDE project output paths.
 for rel in ['cpf-batch/worker/build.gradle','cpf-batch/control-plane/build.gradle']:
  if not (root/rel).is_file(): errors.append(rel+':missing')
 # Compose YAML syntax is source-controlled and should not depend on editor recovery.
 compose=root/'cpf-tools/environment/docker-development-test/compose.yml'
 if yaml:
  try: yaml.safe_load(compose.read_text(encoding='utf-8'))
  except Exception as e: errors.append('compose.yml invalid YAML: '+str(e))
 else:
  # conservative indentation guard around root volumes key
  lines=compose.read_text(encoding='utf-8').splitlines()
  if not any(x=='volumes:' for x in lines): errors.append('compose root volumes missing')
 result={'status':'PASS' if not errors else 'FAIL','errors':errors,'note':'IDE Problems=0 requires Fresh Gradle Sync in user Java25/VSCode environment'}
 print(json.dumps(result,ensure_ascii=False));return 0 if not errors else 1
if __name__=='__main__':raise SystemExit(main())
