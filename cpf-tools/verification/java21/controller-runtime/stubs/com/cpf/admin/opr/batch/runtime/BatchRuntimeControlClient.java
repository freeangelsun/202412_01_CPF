package com.cpf.admin.opr.batch.runtime;
import java.util.*;
class BatchRuntimeControlClient {
  Map<String,Object> last; RuntimeException failure; String lastMethod;
  List<Map<String,Object>> instances(long s){check();return List.of();}
  Map<String,Object> view(String v){check();return Map.of("items",List.of());}
  List<Map<String,Object>> jobDefinitions(String j,String s,int l){check();return List.of();}
  Map<String,Object> jobDefinitionDetail(String j,long v){check();return Map.of();}
  Map<String,Object> validateJobDefinition(Map<String,Object> r){check();return Map.of("valid",true);}
  Map<String,Object> saveJobDefinition(Map<String,Object> r){last=r;lastMethod="save";check();return Map.of("state","DRAFT");}
  Map<String,Object> transitionJobDefinition(String j,long v,Map<String,Object> r){last=r;lastMethod="transition";check();return Map.of("state","PUBLISHED");}
  Map<String,Object> command(Map<String,Object> r){last=r;lastMethod="command";check();return Map.of("state","ACCEPTED");}
  Map<String,Object> commandState(String k){check();return Map.of("state","ACCEPTED");}
  Map<String,Object> createPlan(Map<String,Object> r){last=r;lastMethod="plan";check();return Map.of("state","CREATED");}
  void check(){if(failure!=null)throw failure;}
}
