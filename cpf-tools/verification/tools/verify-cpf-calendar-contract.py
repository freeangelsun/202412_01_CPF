#!/usr/bin/env python3
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse, json
from pathlib import Path

FILES = {
    "service": "cpf-common/src/main/java/com/cpf/common/calendar/CmnCalendarService.java",
    "jdbc": "cpf-common/src/main/java/com/cpf/common/calendar/CmnJdbcCalendarStore.java",
    "insertSql": "cpf-common/src/main/resources/cpf-sql/cmn/calendar/insert.sql",
    "updateSql": "cpf-common/src/main/resources/cpf-sql/cmn/calendar/update.sql",
    "deleteSql": "cpf-common/src/main/resources/cpf-sql/cmn/calendar/delete.sql",
    "controller": "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmBusinessCalendarController.java",
    "frontend": "cpf-admin/frontend/src/features/business-calendar/BusinessCalendarPage.vue",
}

def verify(root: Path) -> dict[str, object]:
    errors=[]; text={}
    for key,rel in FILES.items():
        p=root/rel
        if not p.is_file(): errors.append(f"missing:{rel}"); text[key]=""
        else: text[key]=p.read_text(encoding="utf-8")
    svc,jdbc,insert_sql,update_sql,delete_sql,ctl,ui=(text[k] for k in (
        "service","jdbc","insertSql","updateSql","deleteSql","controller","frontend"))
    compact_svc="".join(svc.split())
    compact_jdbc="".join(jdbc.split())
    compact_ctl="".join(ctl.split())
    compact_ui="".join(ui.split())
    checks={
      "canonicalOwner": all(FILES[key].startswith("cpf-common/") for key in ("service","jdbc","insertSql","updateSql","deleteSql")),
      "productActorRequired": "if(productMode)thrownewIllegalStateException(\"ProductCalendarmutation은operatorIdoverload가필수입니다.\")" in compact_svc,
      "findDayAvailable": "findDay(StringcalendarId,LocalDatedate)" in compact_svc,
      "jdbcNoSystemFallback": "save(day,expectedVersion,\"SYSTEM\")" not in compact_jdbc and "delete(calendarId,businessDate,expectedVersion,\"SYSTEM\")" not in compact_jdbc,
      "actorAwareMutations": "actorAwareMutations" in jdbc and "required(operatorId" in jdbc,
      "createdUpdatedActor": "created_by,updated_by" in insert_sql and "?,?,?,?,?,?,1,?,?,CURRENT_TIMESTAMP" in insert_sql,
      "createRaceMapped": "catch(DuplicateKeyException" in jdbc and "CREATE_CONFLICT" in jdbc,
      "updateCas": "version_no=version_no+1" in update_sql and "version_no=?" in update_sql,
      "deleteCas": "DELETE FROM cmn_business_calendar_day" in delete_sql and "version_no=?" in delete_sql and "required(operatorId" in jdbc,
      "actualBeforeAudit": compact_ctl.count("calendarService.findDay(calendarId,businessDate).orElse(null)") >= 2 and "before==null?null:String.valueOf(before)" in compact_ctl,
      "auditReasonRequired": "auditLogService.requireReason" in ctl,
      "conflict409": "HttpStatus.CONFLICT" in ctl and "CmnCalendarConflictException" in ctl,
      "writablePermissionUi": "writable.value&&permission.value.writeAllowed" in compact_ui and "writable.value&&permission.value.deleteAllowed" in compact_ui,
      "frontendConflict409": "e.status===409" in compact_ui,
      "resolveOperationWired": "constoperationForm=ref" in compact_ui and "asyncfunctionresolveBusinessDate" in compact_ui and "admCalendarResolveDate" in ui,
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
