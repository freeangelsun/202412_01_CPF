from __future__ import annotations

import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
COORDINATOR = ROOT / "cpf-batch/control-server/src/main/java/com/cpf/batch/control/compat/CpfBatchRiskCommandCoordinator.java"


def write(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def test_post_action_failures_are_unknown_and_never_failed() -> None:
    assert COORDINATOR.is_file()
    with tempfile.TemporaryDirectory() as tmp:
        base = Path(tmp)
        src = base / "src"
        classes = base / "classes"
        write(src / "com/cpf/batch/control/compat/CpfBatchRiskCommandCoordinator.java", COORDINATOR.read_text(encoding="utf-8"))
        write(src / "com/cpf/core/api/batch/CpfBatchRiskCommand.java", """
package com.cpf.core.api.batch;
public record CpfBatchRiskCommand(String idempotencyKey) {}
""")
        write(src / "com/cpf/core/api/batch/CpfBatchOwnerUnknownResultException.java", """
package com.cpf.core.api.batch;
public class CpfBatchOwnerUnknownResultException extends RuntimeException {
 private final String code;
 public CpfBatchOwnerUnknownResultException(String c,String m){super(m);code=c;}
 public String failureCode(){return code;}
}
""")
        write(src / "com/cpf/core/api/data/CpfDataRow.java", """
package com.cpf.core.api.data;
import java.util.*;
public class CpfDataRow extends LinkedHashMap<String,Object>{
 public static CpfDataRow copyOf(Object value){CpfDataRow r=new CpfDataRow();if(value instanceof Map<?,?>m)m.forEach((k,v)->r.put(String.valueOf(k),v));return r;}
 public static CpfDataRow of(Object...kv){CpfDataRow r=new CpfDataRow();for(int i=0;i<kv.length;i+=2)r.put(String.valueOf(kv[i]),kv[i+1]);return r;}
}
""")
        write(src / "com/cpf/batch/control/compat/JdbcBatchRiskCommandLedger.java", """
package com.cpf.batch.control.compat;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
public class JdbcBatchRiskCommandLedger {
 public enum Kind{CREATED,REPLAY,CONFLICT,IN_PROGRESS,FAILED,UNKNOWN}
 public record Decision(Kind kind,String resultPayload,String code,String message){}
 public Decision next=new Decision(Kind.CREATED,null,null,null);
 public int completed,failed,unknown; public boolean failComplete,failUnknown;
 public Decision reserve(CpfBatchRiskCommand c){return next;}
 public void complete(CpfBatchRiskCommand c,String p){completed++;if(failComplete)throw new IllegalStateException("complete unavailable");}
 public void fail(CpfBatchRiskCommand c,String code,String msg){failed++;}
 public void unknown(CpfBatchRiskCommand c,String code,String msg){unknown++;if(failUnknown)throw new IllegalStateException("unknown unavailable");}
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
        write(src / "com/cpf/batch/control/compat/PostActionUnknownHarness.java", """
package com.cpf.batch.control.compat;
import com.cpf.core.api.batch.*; import com.cpf.core.api.data.*; import com.fasterxml.jackson.databind.ObjectMapper;
public class PostActionUnknownHarness {
 static int checks;
 static void ok(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 static void expectUnknown(String code,Runnable r,JdbcBatchRiskCommandLedger ledger,int failedBefore){
  try{r.run();throw new AssertionError("unknown expected");}
  catch(CpfBatchOwnerUnknownResultException x){ok(code.equals(x.failureCode()),"code");ok(ledger.failed==failedBefore,"must not mark FAILED");}
 }
 public static void main(String[]a){
  CpfBatchRiskCommand cmd=new CpfBatchRiskCommand("idem");
  JdbcBatchRiskCommandLedger serializationLedger=new JdbcBatchRiskCommandLedger();ObjectMapper brokenMapper=new ObjectMapper();brokenMapper.failWrite=true;
  CpfBatchRiskCommandCoordinator serialization=new CpfBatchRiskCommandCoordinator(serializationLedger,brokenMapper);int[] sideEffects={0};
  expectUnknown("RESULT_SERIALIZATION_FAILED",()->serialization.executeRow(cmd,()->{sideEffects[0]++;return CpfDataRow.of("ok",true);}),serializationLedger,0);
  ok(sideEffects[0]==1,"side effect executed once");ok(serializationLedger.unknown==1,"serialization persisted UNKNOWN");

  JdbcBatchRiskCommandLedger finalizeLedger=new JdbcBatchRiskCommandLedger();finalizeLedger.failComplete=true;
  CpfBatchRiskCommandCoordinator finalize=new CpfBatchRiskCommandCoordinator(finalizeLedger,new ObjectMapper());
  expectUnknown("LEDGER_FINALIZATION_FAILED",()->finalize.executeRows(cmd,()->java.util.List.of(CpfDataRow.of("ok",true))),finalizeLedger,0);
  ok(finalizeLedger.completed==1,"complete attempted");ok(finalizeLedger.unknown==1,"finalization persisted UNKNOWN");

  JdbcBatchRiskCommandLedger unavailableLedger=new JdbcBatchRiskCommandLedger();unavailableLedger.failComplete=true;unavailableLedger.failUnknown=true;
  CpfBatchRiskCommandCoordinator unavailable=new CpfBatchRiskCommandCoordinator(unavailableLedger,new ObjectMapper());
  try{unavailable.executeRow(cmd,()->CpfDataRow.of("ok",true));throw new AssertionError("unknown expected");}
  catch(CpfBatchOwnerUnknownResultException x){ok("LEDGER_FINALIZATION_FAILED".equals(x.failureCode()),"code retained");ok(x.getSuppressed().length==1,"ledger failure suppressed");ok(unavailableLedger.failed==0,"never FAILED");}
  System.out.println("PASS assertions="+checks);
 }
}
""")
        sources = [str(path) for path in src.rglob("*.java")]
        compile_result = subprocess.run(["javac", "-d", str(classes), *sources], text=True, capture_output=True)
        assert compile_result.returncode == 0, compile_result.stderr
        run_result = subprocess.run(["java", "-cp", str(classes), "com.cpf.batch.control.compat.PostActionUnknownHarness"], text=True, capture_output=True)
        assert run_result.returncode == 0, run_result.stderr
        assert "PASS assertions=" in run_result.stdout
