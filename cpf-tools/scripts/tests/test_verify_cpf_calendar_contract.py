import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).parents[1]/"verify-cpf-calendar-contract.py"
spec=importlib.util.spec_from_file_location("calendar_gate",SCRIPT);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
BASE={
"service":'''class X { Object findDay(String calendarId,LocalDate date){return null;} Object save(Object d,long v){if(productMode)throw new IllegalStateException("Product Calendar mutation은 operatorId overload가 필수입니다.");return null;} }''',
"jdbc":'''class X { void x(){ String q="created_by,updated_by updated_by=? version_no=version_no+1 AND version_no=? DELETE FROM cmn_business_calendar_day"; Object x=actor,actor; required(operatorId); } void y(){try{}catch(DuplicateKeyException e){String s="CREATE_CONFLICT";}} }''',
"controller":'''class X { void a(){calendarService.findDay(calendarId,businessDate).orElse(null);calendarService.findDay(calendarId,businessDate).orElse(null);String x=before==null?null:String.valueOf(before);auditLogService.requireReason(x);String a="CmnCalendarConflictException";String b="HttpStatus.CONFLICT";} }''',
"frontend":'''const canWrite=computed(()=>writable.value&&permission.value.writeAllowed);const canDelete=computed(()=>writable.value&&permission.value.deleteAllowed);if(e.status===409){} const operationForm=ref({}); async function resolveBusinessDate(){cpfApi(`/resolve?date=${x}`)}'''}
PATHS=m.FILES
class T(unittest.TestCase):
 def root(self,replace=None):
  td=tempfile.TemporaryDirectory();self.addCleanup(td.cleanup);r=Path(td.name)
  data=dict(BASE)
  if replace:data.update(replace)
  for k,rel in PATHS.items():p=r/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(data[k], encoding="utf-8")
  return r
 def test_pass(self):self.assertEqual("PASS",m.verify(self.root())["status"])
 def test_rejects_system_actor(self):
  with self.assertRaises(SystemExit):m.verify(self.root({"jdbc":BASE["jdbc"]+' save(day,expectedVersion,"SYSTEM")'}))
 def test_rejects_missing_before(self):
  with self.assertRaises(SystemExit):m.verify(self.root({"controller":BASE["controller"].replace('calendarService.findDay(calendarId,businessDate).orElse(null);','',1)}))
 def test_rejects_unwired_resolve(self):
  with self.assertRaises(SystemExit):m.verify(self.root({"frontend":BASE["frontend"].replace('async function resolveBusinessDate','async function missing')}))
if __name__=='__main__':unittest.main()
