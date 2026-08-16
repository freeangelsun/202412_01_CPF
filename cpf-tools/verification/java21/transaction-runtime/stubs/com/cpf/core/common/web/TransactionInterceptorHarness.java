package com.cpf.core.common.web;
import com.fasterxml.jackson.databind.ObjectMapper; import com.cpf.foundation.context.header.*; import jakarta.servlet.http.*; import org.springframework.beans.factory.ObjectProvider; import org.springframework.core.env.Environment; import org.springframework.web.method.HandlerMethod; import java.io.*; import java.lang.reflect.*; import java.util.*;
public final class TransactionInterceptorHarness {
 static int assertions=0; static void check(boolean x,String m){assertions++;if(!x)throw new AssertionError(m);}
 static class P<T> implements ObjectProvider<T>{final T v;P(T v){this.v=v;}public T getIfAvailable(){return v;}}
 static class E implements Environment {}
 static class Req implements HttpServletRequest {Map<String,String>h=new LinkedHashMap<>();String uri="/business";String remote="127.0.0.1";public String getHeader(String n){return h.get(n);}public Enumeration<String> getHeaderNames(){return Collections.enumeration(h.keySet());}public String getRequestURI(){return uri;}public String getRemoteAddr(){return remote;}public Object getAttribute(String n){return null;}Req valid(){h.put(CpfHeaderNames.TRANSACTION_ID,"ABC12345-1234567");h.put(CpfHeaderNames.REQUEST_TYPE,"SYNC");h.put(CpfHeaderNames.ORIGINAL_CHANNEL_CODE,"WEB");h.put(CpfHeaderNames.CHANNEL_CODE,"WEB");return this;}}
 static class Res implements HttpServletResponse {int status;Map<String,String>h=new HashMap<>();StringWriter w=new StringWriter();public void setStatus(int s){status=s;}public void setContentType(String s){}public void setCharacterEncoding(String s){}public void setHeader(String n,String v){h.put(n,v);}public Writer getWriter(){return w;}}
 static class C {public void plain(){} @com.cpf.foundation.execution.api.CpfOnlineTransaction(id="O-12345678",name="online") public void online(){} @com.cpf.foundation.execution.api.CpfSharedApi(id="S-12345678",name="shared",allowedCallers={"PAY"}) public void shared(){}}
 static HandlerMethod hm(String n)throws Exception{C c=new C();return new HandlerMethod(c,C.class.getMethod(n));}
 public static void main(String[]a)throws Exception{
  var i=new TransactionHeaderValidationInterceptor(new ObjectMapper(),new P<>(null),new E());
  var r1=new Req();var s1=new Res();check(!i.preHandle(r1,s1,hm("plain")),"unannotated business missing must block");check(s1.status==400,"status");
  var r2=new Req().valid();var s2=new Res();check(i.preHandle(r2,s2,hm("plain")),"unannotated valid must pass");
  var r3=new Req();r3.uri="/actuator/health";var s3=new Res();check(i.preHandle(r3,s3,hm("plain")),"actuator allowlist");
  var r4=new Req();r4.uri="/v3/api-docs";check(i.preHandle(r4,new Res(),hm("plain")),"api docs allowlist");
  var r5=new Req().valid();r5.h.put("X-Cpf-Ext-Api-Token","raw");check(!i.preHandle(r5,new Res(),hm("plain")),"sensitive ext block");
  var r6=new Req().valid();check(i.preHandle(r6,new Res(),hm("online")),"online valid external");
  var r7=new Req().valid();r7.h.put(CpfHeaderNames.CALLER_SERVICE,"PAY");r7.h.put(CpfHeaderNames.CALLER_INSTANCE_ID,"P1");check(!i.preHandle(r7,new Res(),hm("online")),"internal missing execution block");
  var r8=new Req().valid();r8.h.put(CpfHeaderNames.CALLER_SERVICE,"PAY");r8.h.put(CpfHeaderNames.CALLER_INSTANCE_ID,"P1");r8.h.put(CpfHeaderNames.STANDARD_EXECUTION_ID,"O-12345678");check(i.preHandle(r8,new Res(),hm("online")),"internal exact execution pass");
  var r9=new Req().valid();r9.h.put(CpfHeaderNames.CALLER_SERVICE,"PAY");r9.h.put(CpfHeaderNames.CALLER_INSTANCE_ID,"P1");r9.h.put(CpfHeaderNames.STANDARD_EXECUTION_ID,"S-12345678");check(i.preHandle(r9,new Res(),hm("shared")),"shared trusted loopback pass");
  var r10=new Req().valid();r10.remote="10.0.0.9";r10.h.put(CpfHeaderNames.CALLER_SERVICE,"PAY");r10.h.put(CpfHeaderNames.CALLER_INSTANCE_ID,"P1");r10.h.put(CpfHeaderNames.STANDARD_EXECUTION_ID,"S-12345678");check(!i.preHandle(r10,new Res(),hm("shared")),"untrusted shared block");
  System.out.println("PASS assertions="+assertions+" actualInterceptor=true actualInboundValidator=true");
 }
}
