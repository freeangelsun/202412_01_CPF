#!/usr/bin/env python3
"""외부연계 Closure의 Source·DB3·ADM Consumer 정합성을 검증한다."""
from pathlib import Path
import argparse,json,re,sys
ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('positional_root',nargs='?'); ns=ap.parse_args()
root=Path(ns.root if ns.root!='.' or not ns.positional_root else ns.positional_root).resolve()
required=[
'cpf-core/src/main/java/com/cpf/core/api/security/crypto/CpfCryptoOperations.java',
'cpf-starters/security/src/main/java/com/cpf/security/common/security/crypto/JceCpfCryptoOperations.java',
'cpf-starters/base/runtime/src/main/java/com/cpf/foundation/time/CpfTimeOperations.java',
'cpf-starters/base/runtime/src/main/java/com/cpf/foundation/time/CpfLeaseTimeService.java',
'cpf-core/src/main/java/com/cpf/core/api/data/encryption/CpfFieldEncryptionOperations.java',
'cpf-starters/security/src/main/java/com/cpf/security/crypto/data/CpfProtectedRecordService.java',
'cpf-starters/data/src/main/java/com/cpf/data/api/quality/CpfDataQualityOperations.java',
'cpf-starters/data/src/main/java/com/cpf/data/quality/InMemoryCpfDataQualityOperations.java',
'cpf-starters/integration/src/main/java/com/cpf/integration/api/webhook/CpfWebhookOperations.java',
'cpf-starters/integration/webhook/src/main/java/com/cpf/integration/webhook/InMemoryCpfWebhookOperations.java',
'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmIntegrationClosureController.java',
'cpf-admin/frontend/src/generated/cpf-api.ts',
'cpf-admin/frontend/src/features/integration-closure/integrationClosureApi.ts',
'cpf-admin/frontend/src/app/methods/integrationClosureMethods.ts',
'cpf-tools/generator/templates/webhook/CpfGeneratedWebhookAdapter.java.tpl']
missing=[p for p in required if not (root/p).is_file()]
vendors=[]
for v in ('oracle','postgresql','mariadb'):
 b=root/f'cpf-tools/db/vendor/{v}'; paths=['source/17_integration_closure_lifecycle.sql','install/06_integration_closure_lifecycle.sql','migration/V102__integration_closure_lifecycle.sql','rollback/R102__integration_closure_lifecycle.sql','verify/102_verify_integration_closure_lifecycle.sql','runtime/integration-closure/integration_closure_queries.sql','pack.json']; vendors.append((v,all((b/p).is_file() for p in paths),paths));
 pack=json.loads((b/'pack.json').read_text());
 if int(pack.get('schemaVersion',0)) < 5: raise AssertionError(f'{v}:pack schemaVersion<5')
 for key in ('canonicalSchema','generatedCurrentRoot','historicalMigrationRoot','runtimeRoot'):
  if not isinstance(pack.get(key),str) or not pack[key].strip(): raise AssertionError(f'{v}:pack missing {key}')
catalog=json.loads((root/'cpf-tools/generator/contracts/cpf-starter-catalog.json').read_text()); webhook=[m for m in catalog['modules'] if m.get('projectPath')==':internal:integration:webhook']
settings=(root/'settings.gradle').read_text(); route=(root/'cpf-admin/frontend/src/app/methods/routeClosureMethods.ts').read_text(); generated=(root/'cpf-admin/frontend/src/generated/cpf-api.ts').read_text(); facade=(root/'cpf-admin/frontend/src/features/integration-closure/integrationClosureApi.ts').read_text(); app_methods=(root/'cpf-admin/frontend/src/app/methods/integrationClosureMethods.ts').read_text();
checks={'missing':missing,'vendorParity':{v:ok for v,ok,_ in vendors},'webhookCatalogExactlyOne':len(webhook)==1,'settingsCatalogDriven':'cpf-starter-catalog.json' in settings,'routeRawAbsoluteUrlAbsent':not re.search(r'https?://',route),'generatedClientRelativeRoutes':not any(token in generated for token in ('fetch(\"http://',"fetch('http://",'fetch(\"https://',"fetch('https://")),'typedIntegrationFacade':'../../generated/cpf-api' in facade and 'integrationClosureApi' in app_methods and 'approved: boolean' not in app_methods,'coreProviderBoundary':not any('javax.crypto' in (root/p).read_text(encoding='utf-8',errors='replace') for p in required if p.startswith('cpf-core/') and (root/p).is_file())}
print(json.dumps(checks,ensure_ascii=False,indent=2));sys.exit(0 if not missing and all(checks['vendorParity'].values()) and all(v for k,v in checks.items() if k not in ('missing','vendorParity')) else 1)
