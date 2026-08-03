package com.cpf.batch.control.internal;
import com.cpf.batch.api.*;
import java.util.*;
public class JdbcRuntimeCommandRepository {
 public Map<String,Object> create(RuntimeCommand c){return Map.of();}
 public boolean beginExecution(String commandId){return false;}
 public Optional<Map<String,Object>> find(String key){return Optional.empty();}
 public void transition(String id, CommandState state, String stage, String result){}
 public void recordAttempt(String id,int attempt,String target,String stage,CommandState state,String message){}
}
