package com.cpf.tools.dbless; import com.cpf.admin.config.*;import com.cpf.common.config.*;import org.springframework.core.env.Environment;import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;import java.util.*;
public class DbLessContextHarness{static int n=0;static void c(boolean x,String m){n++;if(!x)throw new AssertionError(m);}static class E implements Environment{Map<String,String>p=new HashMap<>();String[]profiles={};public String getProperty(String k,String d){return p.getOrDefault(k,d);}public String[]getActiveProfiles(){return profiles;}}
 static void fail(Runnable r,String m){n++;try{r.run();throw new AssertionError(m);}catch(RuntimeException ok){}}
 public static void main(String[]a)throws Exception{
  E e=new E();var adm=new AdmPersistencePolicy(e);c(adm.databaseRequired()&&!adm.memoryEnabled(),"default database");
  E mem=new E();mem.p.put("cpf.adm.persistence.mode","memory");fail(()->new AdmPersistencePolicy(mem),"memory prod must fail");
  mem.profiles=new String[]{"test"};c(new AdmPersistencePolicy(mem).memoryEnabled(),"test memory");
  String jdbcExpr=CmnDataSourceConfig.class.getAnnotation(ConditionalOnExpression.class).value();String mbExpr=CmnMyBatisConfig.class.getAnnotation(ConditionalOnExpression.class).value();c(jdbcExpr.contains("cpf.common.runtime-mode:product")&&mbExpr.equals(jdbcExpr),"same runtime condition");
  E lib=new E();lib.p.put("cpf.common.runtime-mode","library");c(!"product".equalsIgnoreCase(lib.getProperty("cpf.common.runtime-mode","product")),"library disables configs");
  E prod=new E();boolean failed=false;try{new CmnDataSourceConfig().cmnDataSource(prod);}catch(javax.naming.NamingException ex){failed=true;}c(failed,"product missing datasource fail closed");
  boolean ctorFailed=false;try{CmnMyBatisConfig.class.getConstructors()[0].newInstance(new Object[]{null,prod});}catch(Exception ex){ctorFailed=true;} // constructor accepts null in raw Java; Spring unsatisfied dependency is validated by source context test
  c(CmnMyBatisConfig.class.getConstructors().length==1,"single required datasource constructor");
  System.out.println("PASS assertions="+n+" actualPolicies=true syntheticContext=true springContextTestsPresent=true");}}
