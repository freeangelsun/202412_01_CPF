#!/usr/bin/env python3
from pathlib import Path
import argparse,re

def main()->int:
 ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ns=ap.parse_args(); root=Path(ns.root).resolve(); errors=[]
 def txt(rel):
  p=root/rel
  if not p.is_file(): errors.append(f'missing {rel}'); return ''
  return p.read_text(encoding='utf-8-sig',errors='replace')
 api={
  'CpfCodeService':'cpf-common/src/main/java/com/cpf/common/code/api/CpfCodeService.java',
  'CpfMessageSource':'cpf-common/src/main/java/com/cpf/common/message/api/CpfMessageSource.java',
  'CpfParameterService':'cpf-common/src/main/java/com/cpf/common/parameter/api/CpfParameterService.java',
  'CpfCalendarService':'cpf-common/src/main/java/com/cpf/common/calendar/api/CpfCalendarService.java',
  'CpfTemplateService':'cpf-common/src/main/java/com/cpf/common/template/api/CpfTemplateService.java',
 }
 required={
  'CpfCodeService':['values(','find(','required('],
  'CpfMessageSource':['getMessage(String messageCode, Locale locale, Map','getMessage(String messageCode, Locale locale)'],
  'CpfParameterService':['find(String key)','requiredValue(String key)','findValue(String key, Class<T> type)','requiredValue(String key, Class<T> type)'],
  'CpfCalendarService':['isBusinessDay(','shiftBusinessDay(','nextBusinessDay(','previousBusinessDay('],
  'CpfTemplateService':['render(String templateCode, String channel, Map'],
 }
 for name,rel in api.items():
  s=txt(rel)
  if f'public interface {name}' not in s: errors.append(f'{name}: canonical public interface missing')
  for token in required[name]:
   if token not in s: errors.append(f'{name}: missing ergonomic operation {token}')
 for name in ('CpfCodeService','CpfParameterService'):
  s=txt(api[name])
  if 'void refresh();' in s and '@Deprecated' not in s: errors.append(f'{name}: refresh must not be Golden Path')
 mgmt=txt('cpf-common/src/main/java/com/cpf/common/message/api/CpfCommonCatalogManagementService.java')
 if 'refreshCaches(String actor, String reason)' not in mgmt: errors.append('Common management refresh contract missing')
 compat=txt('cpf-common/src/main/java/com/cpf/common/calendar/CmnBusinessCalendar.java')
 if 'extends CpfCalendarService' not in compat or '@Deprecated' not in compat: errors.append('Calendar compatibility alias must delegate/deprecate')
 edu_paths=list((root/'cpf-education/src/main/java/com/cpf/education/online').rglob('*.java'))+list((root/'cpf-education/src/main/java/com/cpf/education/batch').rglob('*.java'))
 edu='\n'.join(p.read_text(encoding='utf-8-sig',errors='replace') for p in edu_paths)
 for name in ('CpfCodeService','CpfMessageSource','CpfParameterService','CpfCalendarService'):
  if name not in edu: errors.append(f'Canonical Education consumer missing {name}')
 if 'MDC.put' in edu: errors.append('Canonical Education must not rebuild logging MDC manually')
 for root_name in ('cpf-education','cpf-member','cpf-external','cpf-backoffice'):
  rp=root/root_name
  if not rp.exists(): continue
  for p in rp.rglob('*.java'):
   body=p.read_text(encoding='utf-8-sig',errors='replace')
   if re.search(r'import\s+com\.cpf\.common\.(?:runtime|code\.service|parameter\.service|template\.service)\.',body):
    errors.append(f'customer-facing consumer bypasses Common public API: {p.relative_to(root)}')
 backoffice=txt('cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/catalog/controller/BackofficeCommonCatalogController.java')
 if 'CpfCommonCatalogManagementService' not in backoffice: errors.append('Backoffice Domain Common management must use public management API')
 for external in ('cpf-backoffice-web',):
  rp=root/external
  if not rp.exists(): continue
  for p in rp.rglob('*'):
   if p.is_file() and p.suffix in ('.java','.ts','.vue'):
    body=p.read_text(encoding='utf-8-sig',errors='replace')
    if re.search(r'(?:import\s+com\.cpf\.common|com\.cpf\.common\.)',body): errors.append(f'external MBW surface must not depend on Common Java API: {p.relative_to(root)}')
 if errors:
  print('CPF_COMMON_PRODUCT_SERVICE_DX=FAIL')
  for i,e in enumerate(errors,1): print(f'{i:03d} {e}')
  return 1
 print('CPF_COMMON_PRODUCT_SERVICE_DX=PASS services=5 goldenPath=5 educationConsumer=PASS managementBoundary=PASS owner=cpf-common')
 return 0
if __name__=='__main__': raise SystemExit(main())
