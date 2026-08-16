#!/usr/bin/env python3
"""DB-less/product fail-closed gate requiring semantic source conditions and executable context tests."""
from __future__ import annotations
import argparse,json
from pathlib import Path
class GateError(RuntimeError):pass
REQ={
 'cpf-admin/src/main/java/com/cpf/admin/config/AdmPersistencePolicy.java':('DATABASE','MEMORY_ALLOWED_PROFILES','getActiveProfiles','CpfValidationException','cpf.adm.persistence.mode'),
 'cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/common/config/CmnDataSourceConfig.java':('ConditionalOnExpression','cpf.common.runtime-mode:product','CpfDataSources.resolve','cmnDataSource'),
 'cpf-starters/data/persistence-mybatis/src/main/java/com/cpf/common/config/CmnMyBatisConfig.java':('ConditionalOnExpression','cpf.common.runtime-mode:product','@Qualifier("cmnDataSource")','setDataSource'),
 'cpf-admin/src/test/java/com/cpf/admin/config/AdmPersistencePolicyContextTest.java':('ApplicationContextRunner','productDefaultRequiresDatabaseAndNeverInventsMemoryPersistence','memoryModeWithoutEduOrTestProfileFailsClosed','explicitTestProfileMayUseMemoryWithoutJdbcBeans','cpf.adm.persistence.mode=memory'),
 'cpf-starters/data/persistence-jdbc/src/test/java/com/cpf/common/config/CmnDataSourceContextTest.java':('ApplicationContextRunner','libraryRuntimeModeDoesNotCreateCmnJdbcBeans','productRuntimeWithoutDatasourceConfigurationFailsClosed','invalidConfiguredDatasourceNeverBecomesAFalseGreenBean','cpf.common.runtime-mode=library'),
 'cpf-starters/data/persistence-mybatis/src/test/java/com/cpf/common/config/CmnMyBatisContextTest.java':('ApplicationContextRunner','libraryRuntimeModeDoesNotCreateCmnMyBatisBeans','productRuntimeWithoutCmnDatasourceFailsClosed','cpf.common.runtime-mode=library'),
}

def verify(root:Path):
 findings=[];files=[]
 texts={}
 for rel,toks in REQ.items():
  p=root/rel
  if not p.is_file():findings.append(f'missing {rel}');continue
  t=p.read_text(encoding='utf-8-sig');texts[rel]=t;files.append(rel)
  for x in toks:
   if x not in t:findings.append(f'{rel}: missing {x}')
 adm=texts.get('cpf-admin/src/main/java/com/cpf/admin/config/AdmPersistencePolicy.java','')
 if '"DATABASE"' not in adm or 'Mode.MEMORY' not in adm:
  findings.append('ADM persistence default/restricted memory policy is not explicit')
 if any(x in adm for x in ('ConcurrentHashMap','new HashMap','InMemory')):
  findings.append('ADM product policy must not create an in-memory persistence fallback')
 for rel in ('cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/common/config/CmnDataSourceConfig.java','cpf-starters/data/persistence-mybatis/src/main/java/com/cpf/common/config/CmnMyBatisConfig.java'):
  t=texts.get(rel,'')
  if "== 'product'" not in t:
   findings.append(f'{rel}: product-only runtime condition missing')
  if any(x in t for x in ('EmbeddedDatabase','HikariDataSource()','DriverManagerDataSource()')):
   findings.append(f'{rel}: invented fallback datasource is forbidden')
 result={'status':'PASS' if not findings else 'FAIL','scannedFiles':files,'runtimeContextTests':3,'findings':findings}
 if findings:raise GateError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main():
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--json-output');a=p.parse_args();root=Path(a.root).resolve()
 try:r=verify(root);c=0
 except Exception as e:
  try:r=json.loads(str(e))
  except:r={'status':'FAIL','message':str(e)}
  c=1
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else root/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False));return c
if __name__=='__main__':raise SystemExit(main())
