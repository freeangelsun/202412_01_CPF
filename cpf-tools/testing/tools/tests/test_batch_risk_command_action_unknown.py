from __future__ import annotations

import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
COORDINATOR = ROOT / "cpf-batch/control-plane/src/main/java/com/cpf/batch/control/compat/CpfBatchRiskCommandCoordinator.java"


def write(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def test_action_unknown_is_normalized_and_classification_failure_is_fail_closed() -> None:
    assert COORDINATOR.is_file()
    with tempfile.TemporaryDirectory() as tmp:
        base = Path(tmp)
        src = base / "src"
        classes = base / "classes"
        write(src / "com/cpf/batch/control/compat/CpfBatchRiskCommandCoordinator.java", COORDINATOR.read_text(encoding="utf-8"))
        write(src / "com/cpf/batch/api/CpfBatchRiskCommand.java", """
package com.cpf.batch.api;
public record CpfBatchRiskCommand(String idempotencyKey) {}
""")
        write(src / "com/cpf/batch/api/CpfBatchOwnerUnknownResultException.java", """
package com.cpf.batch.api;
public class CpfBatchOwnerUnknownResultException extends RuntimeException {
 private final String code;
 public CpfBatchOwnerUnknownResultException(String c,String m){super(m);code=c;}
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
 public Decision next=new Decision(Kind.CREATED,null,null,null);
 public int completed,failed,unknown; public boolean failComplete,failUnknown,failFailed;
 public Decision reserve(CpfBatchRiskCommand c){return next;}
 public void complete(CpfBatchRiskCommand c,String p){completed++;if(failComplete)throw new IllegalStateException("complete unavailable");}
 public void fail(CpfBatchRiskCommand c,String code,String msg){failed++;if(failFailed)throw new IllegalStateException("failed-state unavailable");}
 public void unknown(CpfBatchRiskCommand c,String code,String msg){unknown++;if(failUnknown)throw new IllegalStateException("unknown-state unavailable");}
}
""")
        write(src / "com/fasterxml/jackson/core/JsonProcessingException.java", """
package com.fasterxml.jackson.core; public class JsonProcessingException extends Exception {public JsonProcessingException(String m){super(m);}}
""")
        write(src / "com/fasterxml/jackson/core/type/TypeReference.java", """
package com.fasterxml.jackson.core.type; public abstract class TypeReference<T>{}
""")
        write(src / "com/fasterxml/jackson/databind/ObjectMapper.java", """
package com.fasterxml.jackson.databind;
import com.fasterxml.jackson.core.*; import com.fasterxml.jackson.core.type.*;
public class ObjectMapper {
 public boolean failWrite;
 public String writeValueAsString(Object v)throws JsonProcessingException{if(failWrite)throw new JsonProcessingException("encode");return "{}";}
 @SuppressWarnings("unchecked") public <T>T readValue(String s,TypeReference<T> t)throws JsonProcessingException{return null;}
}
""")
        write(src / "org/springframework/stereotype/Component.java", "package org.springframework.stereotype; public @interface Component {}")
        for name in ("DataAccessResourceFailureException", "QueryTimeoutException", "RecoverableDataAccessException", "TransientDataAccessException"):
            write(src / f"org/springframework/dao/{name}.java", f"package org.springframework.dao; public class {name} extends RuntimeException{{public {name}(String m){{super(m);}}}}")
        write(src / "com/cpf/batch/control/compat/ActionUnknownHarness.java", """
package com.cpf.batch.control.compat;
import com.cpf.batch.api.*; import com.cpf.data.api.*; import com.fasterxml.jackson.databind.ObjectMapper; import org.springframework.dao.*;
public class ActionUnknownHarness {
 static int checks;
 static void ok(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 public static void main(String[]a){
  CpfBatchRiskCommand cmd=new CpfBatchRiskCommand("idem");

  JdbcBatchRiskCommandLedger transientLedger=new JdbcBatchRiskCommandLedger();
  CpfBatchRiskCommandCoordinator transientCoordinator=new CpfBatchRiskCommandCoordinator(transientLedger,new ObjectMapper());
  try{transientCoordinator.executeRow(cmd,()->{throw new DataAccessResourceFailureException("owner connection lost");});throw new AssertionError("unknown expected");}
  catch(CpfBatchOwnerUnknownResultException x){
   ok("DataAccessResourceFailureException".equals(x.failureCode()),"transient code normalized");
   ok(x.getCause() instanceof DataAccessResourceFailureException,"original transient cause retained");
   ok(transientLedger.unknown==1,"UNKNOWN persisted");
   ok(transientLedger.failed==0,"never FAILED");
  }

  JdbcBatchRiskCommandLedger unavailableUnknownLedger=new JdbcBatchRiskCommandLedger(); unavailableUnknownLedger.failUnknown=true;
  CpfBatchRiskCommandCoordinator unavailableUnknown=new CpfBatchRiskCommandCoordinator(unavailableUnknownLedger,new ObjectMapper());
  try{unavailableUnknown.executeRow(cmd,()->{throw new QueryTimeoutException("timeout");});throw new AssertionError("unknown expected");}
  catch(CpfBatchOwnerUnknownResultException x){
   ok(x.getCause() instanceof QueryTimeoutException,"unknown original cause retained");
   ok(x.getSuppressed().length==1,"unknown persistence failure suppressed");
  }

  JdbcBatchRiskCommandLedger failClassificationLedger=new JdbcBatchRiskCommandLedger(); failClassificationLedger.failFailed=true;
  CpfBatchRiskCommandCoordinator failClassification=new CpfBatchRiskCommandCoordinator(failClassificationLedger,new ObjectMapper());
  try{failClassification.executeRow(cmd,()->{throw new IllegalArgumentException("deterministic owner failure");});throw new AssertionError("classification unknown expected");}
  catch(CpfBatchOwnerUnknownResultException x){
   ok("LEDGER_FAILURE_CLASSIFICATION_FAILED".equals(x.failureCode()),"classification code");
   ok(x.getCause() instanceof IllegalArgumentException,"deterministic cause retained");
   ok(x.getSuppressed().length==1,"ledger classification failure suppressed");
  }

  JdbcBatchRiskCommandLedger deterministicLedger=new JdbcBatchRiskCommandLedger();
  CpfBatchRiskCommandCoordinator deterministic=new CpfBatchRiskCommandCoordinator(deterministicLedger,new ObjectMapper());
  try{deterministic.executeRow(cmd,()->{throw new IllegalArgumentException("invalid input");});throw new AssertionError("original expected");}
  catch(IllegalArgumentException x){ok("invalid input".equals(x.getMessage()),"deterministic original rethrown");ok(deterministicLedger.failed==1,"FAILED persisted");}

  System.out.println("PASS assertions="+checks);
 }
}
""")
        sources = [str(path) for path in src.rglob("*.java")]
        compile_result = subprocess.run(["javac", "-d", str(classes), *sources], text=True, capture_output=True)
        assert compile_result.returncode == 0, compile_result.stderr
        run_result = subprocess.run(["java", "-cp", str(classes), "com.cpf.batch.control.compat.ActionUnknownHarness"], text=True, capture_output=True)
        assert run_result.returncode == 0, run_result.stderr
        assert "PASS assertions=11" in run_result.stdout
