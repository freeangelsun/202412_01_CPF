package com.cpf.tools.verification.qa38;

import com.cpf.core.api.broker.*;
import com.cpf.starter.messaging.reliability.*;
import com.cpf.starter.security.identity.*;
import java.time.*;
import java.util.*;

public final class QA38MessagingIdentityHarness {
  static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
  static void expect(Class<? extends Throwable> type,Runnable action,String name){try{action.run();throw new AssertionError("expected "+name);}catch(Throwable ex){if(!type.isInstance(ex))throw ex;}}
  static CpfBrokerClient client(String prefix){return request->new CpfBrokerPublishResult(prefix+":"+request.messageId(),request.messageId(),prefix,request.key(),Instant.now(),"");}
  static CpfBrokerPublishRequest request(String id){return new CpfBrokerPublishRequest(id,"qa38.topic",id,new byte[]{1},"application/octet-stream","tx","seg","QA38","QA38",id,Map.of(),Map.of());}
  public static void main(String[] args){router();compatibility();identity();System.out.println("QA38_MESSAGING_IDENTITY_HARNESS_PASS");}
  static void router(){
    var router=new CpfBrokerClientRouter(List.of(new CpfNamedBrokerClient("kafka","KAFKA",true,client("K")),new CpfNamedBrokerClient("rabbit","RABBITMQ",false,client("R"))));
    check(router.enqueue(request("1")).status().equals("K:1"),"default binding");
    check(router.enqueue("rabbit",request("2")).status().equals("R:2"),"named binding");
    expect(IllegalStateException.class,()->new CpfBrokerClientRouter(List.of(new CpfNamedBrokerClient("a","KAFKA",false,client("a")),new CpfNamedBrokerClient("b","RABBITMQ",false,client("b")))),"ambiguous binding");
    expect(IllegalStateException.class,()->new CpfBrokerClientRouter(List.of(new CpfNamedBrokerClient("a","KAFKA",true,client("a")),new CpfNamedBrokerClient("a","RABBITMQ",false,client("b")))),"duplicate binding");
  }
  static void compatibility(){
    var guard=new CpfMessageCompatibilityGuard(Map.of("order",Set.of("1","2")));
    guard.verify(Map.of("cpf-schema-id","order","cpf-schema-version","2"));
    expect(CpfMessageCompatibilityGuard.QuarantineException.class,()->guard.verify(Map.of("cpf-schema-id","order","cpf-schema-version","3")),"schema quarantine");
    expect(CpfMessageCompatibilityGuard.QuarantineException.class,()->guard.verify(Map.of()),"missing schema");
  }
  static CpfServiceIdentityProperties properties(String activeId,String activeSecret){
    var p=new CpfServiceIdentityProperties();p.setEnabled(true);p.setServiceId("ADM");p.setActiveKeyId(activeId);p.setActiveSecret(activeSecret);p.setTtl(Duration.ofSeconds(60));p.setClockSkew(Duration.ZERO);return p;
  }
  static void identity(){
    var at=Instant.parse("2026-08-02T00:00:00Z");var oldProps=properties("k1","01234567890123456789012345678901");
    var oldService=new CpfServiceIdentityTokenService(oldProps,Clock.fixed(at,ZoneOffset.UTC));String oldToken=oldService.issue("BATCH","nonce-1");
    var rotated=properties("k2","abcdefghijklmnopqrstuvwxyzABCDEF");rotated.setPreviousKeyId("k1");rotated.setPreviousSecret(oldProps.getActiveSecret());
    var service=new CpfServiceIdentityTokenService(rotated,Clock.fixed(at.plusSeconds(30),ZoneOffset.UTC));var verified=service.verify(oldToken,"BATCH");
    check(verified.serviceId().equals("ADM")&&verified.nonce().equals("nonce-1")&&verified.keyId().equals("k1"),"key rotation");
    expect(SecurityException.class,()->service.verify(oldToken,"GATEWAY"),"audience");
    var expired=new CpfServiceIdentityTokenService(rotated,Clock.fixed(at.plusSeconds(61),ZoneOffset.UTC));expect(SecurityException.class,()->expired.verify(oldToken,"BATCH"),"expiry");
  }
}
