#!/usr/bin/env python3
from pathlib import Path
import re,sys
ROOT=Path(sys.argv[1] if len(sys.argv)>1 else '.').resolve()
errors=[]
def txt(rel):
 p=ROOT/rel
 if not p.is_file(): errors.append(f'missing {rel}'); return ''
 return p.read_text(encoding='utf-8-sig',errors='replace')
api={
 'CpfCodeService':'cpf-starters/common/src/main/java/com/cpf/common/code/api/CpfCodeService.java',
 'CpfMessageService':'cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfMessageService.java',
 'CpfParameterService':'cpf-starters/common/src/main/java/com/cpf/common/parameter/api/CpfParameterService.java',
 'CpfCalendarService':'cpf-starters/common/src/main/java/com/cpf/common/calendar/api/CpfCalendarService.java',
 'CpfTemplateService':'cpf-starters/common/src/main/java/com/cpf/common/template/api/CpfTemplateService.java',
}
required={
 'CpfCodeService':['values(','find(','required('],
 'CpfMessageService':['resolve(String messageCode, Locale locale, Map','resolve(String messageCode, Locale locale)'],
 'CpfParameterService':['find(String key)','requiredValue(String key)','findValue(String key, Class<T> type)','requiredValue(String key, Class<T> type)'],
 'CpfCalendarService':['isBusinessDay(','shiftBusinessDay(','nextBusinessDay(','previousBusinessDay('],
 'CpfTemplateService':['render(String templateCode, String channel, Map'],
}
for name,rel in api.items():
 s=txt(rel)
 if f'public interface {name}' not in s: errors.append(f'{name}: canonical public interface missing')
 for token in required[name]:
  if token not in s: errors.append(f'{name}: missing ergonomic operation {token}')
# 관리와 업무 조회 Surface를 분리하고 refresh는 호환 경로로만 유지한다.
for name in ('CpfCodeService','CpfParameterService'):
 s=txt(api[name])
 if 'void refresh();' in s and '@Deprecated' not in s: errors.append(f'{name}: refresh must not be Golden Path')
mgmt=txt('cpf-starters/common/src/main/java/com/cpf/common/message/api/CpfCommonCatalogManagementService.java')
if 'refreshCaches(String actor, String reason)' not in mgmt: errors.append('Common management refresh contract missing')
compat=txt('cpf-starters/common/src/main/java/com/cpf/common/calendar/CmnBusinessCalendar.java')
if 'extends CpfCalendarService' not in compat or '@Deprecated' not in compat: errors.append('Calendar compatibility alias must delegate/deprecate')
edu=txt('cpf-education/src/main/java/com/cpf/education/common/cmn/controller/EducationCmnEducationController.java')
for name in api:
 if name not in edu: errors.append(f'Education actual consumer missing {name}')
if 'CpfStructuredLogger' not in edu: errors.append('Education Common sample missing structured business logging')
for root_name in ('cpf-education','cpf-member','cpf-external'):
 rp=ROOT/root_name
 if not rp.exists(): continue
 for p in rp.rglob('*.java'):
  s=p.read_text(encoding='utf-8-sig',errors='replace')
  if re.search(r'import\s+com\.cpf\.common\.(?:runtime|code\.service|parameter\.service|template\.service)\.',s):
   errors.append(f'customer-facing consumer bypasses Common public API: {p.relative_to(ROOT)}')
bza=txt('cpf-biz-admin/src/main/java/com/cpf/bizadmin/commoncatalog/BzaCommonCatalogController.java')
if 'CpfCommonCatalogManagementService' not in bza: errors.append('BZA Common management must use public management API')
if errors:
 print('CPF_COMMON_PRODUCT_SERVICE_DX=FAIL')
 for i,e in enumerate(errors,1): print(f'{i:03d} {e}')
 raise SystemExit(1)
print('CPF_COMMON_PRODUCT_SERVICE_DX=PASS services=5 goldenPath=5 educationConsumer=PASS managementBoundary=PASS')
