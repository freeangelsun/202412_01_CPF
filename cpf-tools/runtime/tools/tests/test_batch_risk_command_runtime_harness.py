from __future__ import annotations

import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
COMMAND = ROOT / "cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchRiskCommand.java"
COORDINATOR = ROOT / "cpf-batch/control-plane/src/main/java/com/cpf/batch/control/compat/CpfBatchRiskCommandCoordinator.java"
LEDGER = ROOT / "cpf-batch/control-plane/src/main/java/com/cpf/batch/control/compat/JdbcBatchRiskCommandLedger.java"


def write(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def test_java21_batch_risk_command_runtime_harness() -> None:
    assert COMMAND.is_file() and COORDINATOR.is_file() and LEDGER.is_file()
    with tempfile.TemporaryDirectory() as tmp:
        base = Path(tmp)
        src = base / "src"
        classes = base / "classes"
        write(src / "com/cpf/batch/api/CpfBatchRiskCommand.java", COMMAND.read_text(encoding="utf-8"))
        write(src / "com/cpf/batch/control/compat/CpfBatchRiskCommandCoordinator.java", COORDINATOR.read_text(encoding="utf-8"))
        write(src / "com/cpf/batch/api/CpfBatchOwnerUnknownResultException.java", """
package com.cpf.batch.api;
public class CpfBatchOwnerUnknownResultException extends RuntimeException {
 private final String code; public CpfBatchOwnerUnknownResultException(String c,String m){super(m);code=c;}
 public String failureCode(){return code;}
}
""")
        write(src / "com/cpf/data/api/CpfDataRow.java", """
package com.cpf.data.api;
import java.util.*;
public class CpfDataRow extends LinkedHashMap<String,Object>{
 public static CpfDataRow copyOf(Object value){CpfDataRow r=new CpfDataRow();if(value instanceof Map<?,?>m)m.forEach((k,v)->r.put(String.valueOf(k),v));return r;}
 public static CpfDataRow of(Object...kv){CpfDataRow r=new CpfDataRow();for(int i=0;i<kv.length;i+=2)r.put(String.valueOf(kv[i]),kv[i+1]);return r;}
}
""")
        write(src / "com/cpf/batch/control/compat/JdbcBatchRiskCommandLedger.java", """
package com.cpf.batch.control.compat;
import com.cpf.batch.api.CpfBatchRiskCommand;
public class JdbcBatchRiskCommandLedger {
 public enum Kind{CREATED,REPLAY,CONFLICT,IN_PROGRESS,FAILED,UNKNOWN}
 public record Decision(Kind kind,String resultPayload,String code,String message){}
 public Decision next=new Decision(Kind.CREATED,null,null,null); public int completed,failed,unknown;
 public Decision reserve(CpfBatchRiskCommand c){return next;}
 public void complete(CpfBatchRiskCommand c,String p){completed++; FakeStore.put(p, com.cpf.data.api.CpfDataRow.of("value","ok"));}
 public void fail(CpfBatchRiskCommand c,String code,String msg){failed++;}
 public void unknown(CpfBatchRiskCommand c,String code,String msg){unknown++;}
}
""")
        write(src / "com/cpf/batch/control/compat/FakeStore.java", """
package com.cpf.batch.control.compat;
public final class FakeStore { public static final java.util.Map<String,Object> V=new java.util.HashMap<>(); public static void put(String k,Object v){V.put(k,v);} }
""")
        write(src / "com/fasterxml/jackson/core/JsonProcessingException.java", """
package com.fasterxml.jackson.core; public class JsonProcessingException extends Exception {public JsonProcessingException(String m){super(m);}}
""")
        write(src / "com/fasterxml/jackson/core/type/TypeReference.java", """
package com.fasterxml.jackson.core.type; public abstract class TypeReference<T>{}
""")
        write(src / "com/fasterxml/jackson/databind/ObjectMapper.java", """
package com.fasterxml.jackson.databind;
import com.fasterxml.jackson.core.*; import com.fasterxml.jackson.core.type.*; import com.cpf.batch.control.compat.*;
public class ObjectMapper { private int n;
 public String writeValueAsString(Object v)throws JsonProcessingException{String k="v"+(++n); FakeStore.V.put(k,v); return k;}
 @SuppressWarnings("unchecked") public <T>T readValue(String s,TypeReference<T> t)throws JsonProcessingException{return (T)FakeStore.V.get(s);}
}
""")
        write(src / "org/springframework/stereotype/Component.java", "package org.springframework.stereotype; public @interface Component {}")
        for name in ("DataAccessResourceFailureException", "QueryTimeoutException", "RecoverableDataAccessException", "TransientDataAccessException"):
            write(src / f"org/springframework/dao/{name}.java", f"package org.springframework.dao; public class {name} extends RuntimeException{{public {name}(String m){{super(m);}}}}")
        write(src / "com/cpf/batch/control/compat/RiskHarness.java", """
package com.cpf.batch.control.compat;
import com.cpf.batch.api.*; import com.cpf.data.api.*; import com.fasterxml.jackson.databind.ObjectMapper;
public class RiskHarness { static int checks;
 static void ok(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 static CpfBatchRiskCommand cmd(String key,String payload){return new CpfBatchRiskCommand("requestStop","bat_execution","7","BATCH_STOP","operator","approved reason","101",key,3L,payload);}
 public static void main(String[]a){
  CpfBatchRiskCommand c=cmd("idem-1",""); ok(c.fingerprint().length()==64,"hash length"); ok(c.fingerprint().equals(cmd("idem-1","").fingerprint()),"stable hash"); ok(!c.fingerprint().equals(cmd("idem-1","x").fingerprint()),"payload changes hash");
  JdbcBatchRiskCommandLedger l=new JdbcBatchRiskCommandLedger(); CpfBatchRiskCommandCoordinator x=new CpfBatchRiskCommandCoordinator(l,new ObjectMapper());
  int[] calls={0}; CpfDataRow first=x.executeRow(c,()->{calls[0]++;return CpfDataRow.of("value","ok");}); ok(calls[0]==1,"created executes once");ok(l.completed==1,"complete persisted");ok("ok".equals(first.get("value")),"result returned");
  FakeStore.V.put("replay",java.util.Map.of("value","old"));l.next=new JdbcBatchRiskCommandLedger.Decision(JdbcBatchRiskCommandLedger.Kind.REPLAY,"replay",null,null); CpfDataRow replay=x.executeRow(c,()->{calls[0]++;return new CpfDataRow();});ok(calls[0]==1,"replay no side effect");ok(Boolean.TRUE.equals(replay.get("idempotentReplay")),"replay flagged");
  l.next=new JdbcBatchRiskCommandLedger.Decision(JdbcBatchRiskCommandLedger.Kind.CONFLICT,null,"C","conflict");try{x.executeRow(c,()->new CpfDataRow());throw new AssertionError();}catch(IllegalArgumentException expected){ok(true,"conflict");}
  l.next=new JdbcBatchRiskCommandLedger.Decision(JdbcBatchRiskCommandLedger.Kind.IN_PROGRESS,null,"P","progress");try{x.executeRow(c,()->new CpfDataRow());throw new AssertionError();}catch(CpfBatchOwnerUnknownResultException expected){ok("P".equals(expected.failureCode()),"in progress unknown");}
  l.next=new JdbcBatchRiskCommandLedger.Decision(JdbcBatchRiskCommandLedger.Kind.CREATED,null,null,null);try{x.executeRow(c,()->{throw new org.springframework.dao.QueryTimeoutException("timeout");});throw new AssertionError();}catch(CpfBatchOwnerUnknownResultException expected){ok(l.unknown==1,"transient becomes unknown");}
  try{x.executeRow(c,()->{throw new IllegalStateException("bad");});throw new AssertionError();}catch(IllegalStateException expected){ok(l.failed==1,"deterministic becomes failed");}
  System.out.println("PASS assertions="+checks);
 }
}
""")
        sources = [str(p) for p in src.rglob("*.java")]
        compile_result = subprocess.run(["javac", "-d", str(classes), *sources], text=True, capture_output=True)
        assert compile_result.returncode == 0, compile_result.stderr
        run_result = subprocess.run(["java", "-cp", str(classes), "com.cpf.batch.control.compat.RiskHarness"], text=True, capture_output=True)
        assert run_result.returncode == 0, run_result.stderr
        assert "PASS assertions=" in run_result.stdout


def test_schema_and_java_ledger_contract_are_closed() -> None:
    ledger_text = LEDGER.read_text(encoding="utf-8")
    assert "bat_operation_request" in ledger_text
    assert "PROPAGATION_REQUIRES_NEW" in ledger_text
    assert "request_hash" in ledger_text
    assert "UNKNOWN" in ledger_text
    canonical = (ROOT / "cpf-tools/db/canonical/platform-schema.json").read_text(encoding="utf-8")
    assert '"currentName": "bat_operation_request"' in canonical
    for vendor in ("mariadb", "postgresql", "oracle"):
        assert (ROOT / f"cpf-tools/db/vendor/{vendor}/migration/V100__bat_operation_request_ledger.sql").is_file()
        assert (ROOT / f"cpf-tools/db/vendor/{vendor}/rollback/R100__bat_operation_request_ledger.sql").is_file()
