package com.cpf.batch.control.internal;
import com.cpf.batch.api.*;
import java.util.*;
public class JdbcRuntimeRegistry {
 public Map<String,Object> snapshot(String id){return Map.of();}
 public long updateDesiredState(String id, DesiredState state, long version){return version+1;}
}
