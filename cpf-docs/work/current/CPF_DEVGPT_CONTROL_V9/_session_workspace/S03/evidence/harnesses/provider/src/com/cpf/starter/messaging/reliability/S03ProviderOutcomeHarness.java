package com.cpf.starter.messaging.reliability;
import com.cpf.core.api.broker.*; import com.cpf.core.common.broker.*; import java.time.*; import java.util.*;
public final class S03ProviderOutcomeHarness {
 private static final Instant NOW=Instant.parse("2026-08-05T04:00:00Z");
 public static void main(String[] args){runtimeIsUnknown();deterministicIsFailed();unsupportedIsUnknown();mismatchIsUnknown();publishedPasses();System.out.println("S03_PROVIDER_OUTCOME_HARNESS PASS cases=5");}
 static void runtimeIsUnknown(){var p=publisher(r->{throw new IllegalStateException("Authorization=Bearer abc.def password=hunter2");});var x=p.publish(env());check("UNKNOWN".equals(x.status()),"runtime unknown");check(!x.detail().contains("abc.def")&&!x.detail().contains("hunter2")&&x.detail().contains("***"),"mask");}
 static void deterministicIsFailed(){var p=publisher(r->{throw new IllegalArgumentException("invalid header");});var x=p.publish(env());check("FAILED".equals(x.status()),"deterministic failed");}
 static void unsupportedIsUnknown(){var p=publisher(r->new CpfBrokerPublishResult("QUEUED_SOMEWHERE",r.messageId(),"x",null,NOW,null));var x=p.publish(env());check("UNKNOWN".equals(x.status()),"unsupported unknown");}
 static void mismatchIsUnknown(){var p=publisher(r->new CpfBrokerPublishResult("PUBLISHED","other","x",null,NOW,null));check("UNKNOWN".equals(p.publish(env()).status()),"mismatch");}
 static void publishedPasses(){var p=publisher(r->new CpfBrokerPublishResult("PUBLISHED",r.messageId(),"x","p0",NOW,null));check("PUBLISHED".equals(p.publish(env()).status()),"published");}
 static CpfProviderBrokerPublisher publisher(CpfBrokerClient c){return new CpfProviderBrokerPublisher(new CpfBrokerClientRouter(List.of(new CpfNamedBrokerClient("default","test",true,c))),Clock.fixed(NOW,ZoneOffset.UTC));}
 static CpfBrokerEnvelope env(){return new CpfBrokerEnvelope("tx","seg","p","c","idem",NOW,new CpfBrokerMessage("m","topic","key",new byte[]{1},"application/json",Map.of()),Map.of());}
 static void check(boolean c,String m){if(!c)throw new AssertionError(m);}
}
