#!/usr/bin/env python3
from __future__ import annotations
import argparse, json
from pathlib import Path

FILES = {
    "service": "cpf-common/src/main/java/com/cpf/common/calendar/CmnCalendarService.java",
    "jdbc": "cpf-common/src/main/java/com/cpf/common/calendar/CmnJdbcCalendarStore.java",
    "controller": "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmBusinessCalendarController.java",
    "frontend": "cpf-admin/frontend/src/features/business-calendar/BusinessCalendarPage.vue",
}

def verify(root: Path) -> dict[str, object]:
    errors=[]; text={}
    for key,rel in FILES.items():
        p=root/rel
        if not p.is_file(): errors.append(f"missing:{rel}"); text[key]=""
        else: text[key]=p.read_text(encoding="utf-8")
    svc,jdbc,ctl,ui=(text[k] for k in ("service","jdbc","controller","frontend"))
    checks={
      "productActorRequired": 'if(productMode)throw new IllegalStateException("Product Calendar mutation은 operatorId overload가 필수입니다.")' in svc,
      "findDayAvailable": "findDay(String calendarId,LocalDate date)" in svc,
      "jdbcNoSystemFallback": 'save(day,expectedVersion,"SYSTEM")' not in jdbc and 'delete(calendarId,businessDate,expectedVersion,"SYSTEM")' not in jdbc,
      "createdUpdatedActor": "created_by,updated_by" in jdbc and "actor,actor" in jdbc and "updated_by=?" in jdbc,
      "createRaceMapped": "catch(DuplicateKeyException" in jdbc and "CREATE_CONFLICT" in jdbc,
      "updateCas": "version_no=version_no+1" in jdbc and "AND version_no=?" in jdbc,
      "deleteCas": "DELETE FROM cmn_business_calendar_day" in jdbc and "version_no=?" in jdbc and "required(operatorId" in jdbc,
      "actualBeforeAudit": ctl.count("calendarService.findDay(calendarId,businessDate).orElse(null)") >= 2 and 'before==null?null:String.valueOf(before)' in ctl,
      "auditReasonRequired": "auditLogService.requireReason" in ctl,
      "conflict409": "HttpStatus.CONFLICT" in ctl and "CmnCalendarConflictException" in ctl,
      "writablePermissionUi": "writable.value&&permission.value.writeAllowed" in ui and "writable.value&&permission.value.deleteAllowed" in ui,
      "frontendConflict409": "e.status===409" in ui,
      "resolveOperationWired": "const operationForm=ref" in ui and "async function resolveBusinessDate" in ui and "/resolve?date=" in ui,
    }
    for k,v in checks.items():
        if not v: errors.append(k)
    result={"status":"PASS" if not errors else "FAIL","checks":checks,"errors":errors}
    print(json.dumps(result,ensure_ascii=False,indent=2))
    if errors: raise SystemExit(1)
    return result

def main():
    p=argparse.ArgumentParser();p.add_argument("--root",default=".");a=p.parse_args();verify(Path(a.root).resolve())
if __name__=="__main__": main()
