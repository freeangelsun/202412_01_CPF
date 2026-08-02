package com.cpf.tools.verification.qa38;

import com.cpf.integration.fixedlength.*;
import com.cpf.starter.iso8583.*;
import com.cpf.starter.tcp.*;
import com.cpf.starter.archive.*;
import com.cpf.starter.notification.*;
import com.cpf.starter.security.identity.*;
import com.cpf.starter.base.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.*;

public class Qa38PureRuntimeHarness {
  private static int checks;
  static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
  static void expect(Class<? extends Throwable> type, Runnable r,String message){checks++;try{r.run();throw new AssertionError("expected "+type.getSimpleName()+": "+message);}catch(Throwable t){if(t instanceof AssertionError)throw (AssertionError)t;if(!type.isInstance(t))throw new AssertionError(message+" wrong exception "+t,t);}}
  static byte[] frame(CpfTcpProperties.Frame kind,byte[] payload,int fixed)throws Exception{
    CpfTcpProperties p=new CpfTcpProperties();p.setEnabled(true);p.setPort(1234);p.setFrame(kind);p.setMaxFrameBytes(1024);if(kind==CpfTcpProperties.Frame.FIXED)p.setFixedLength(fixed);p.validate();
    CpfTcpFrameCodec c=new CpfTcpFrameCodec(p);ByteArrayOutputStream out=new ByteArrayOutputStream();c.write(out,payload);return c.read(new ByteArrayInputStream(out.toByteArray()));
  }
  public static void main(String[] args)throws Exception{
    // Fixed-length + binary codec
    var fields=List.of(new CpfFixedLengthField("name",0,6,CpfFixedLengthField.Alignment.LEFT,' ',true),new CpfFixedLengthField("amount",6,4,CpfFixedLengthField.Alignment.RIGHT,'0',true));
    var layout=new CpfFixedLengthLayout("T",10,Charset.forName("EUC-KR"),fields);
    var fixed=new CpfFixedLengthCodec(layout);var encoded=fixed.encode(Map.of("name","홍길","amount","42"));
    var decoded=fixed.decode(encoded);check(decoded.get("name").equals("홍길"),"EUC-KR fixed name");check(decoded.get("amount").equals("42"),"right pad strip");
    expect(IllegalArgumentException.class,()->fixed.encode(Map.of("name","TOO-LONG","amount","1")),"fixed overflow");
    var binary=new CpfBinaryFieldCodec();check(Arrays.equals(binary.unsigned(0x1234,2,ByteOrder.BIG_ENDIAN),new byte[]{0x12,0x34}),"big endian");check(binary.unsigned(new byte[]{0x34,0x12},ByteOrder.LITTLE_ENDIAN)==0x1234,"little endian");check(binary.packedBcd(binary.packedBcd("12345"),5).equals("12345"),"BCD roundtrip");check(binary.hex(binary.hex("01A0FF")).equals("01A0FF"),"hex roundtrip");

    // ISO8583 primary + secondary bitmap + MAC
    Map<Integer,CpfIso8583FieldSpec> specs=new HashMap<>();specs.put(2,new CpfIso8583FieldSpec(2,19,CpfIso8583FieldSpec.Format.LLVAR,true));specs.put(3,new CpfIso8583FieldSpec(3,6,CpfIso8583FieldSpec.Format.FIXED,true));specs.put(70,new CpfIso8583FieldSpec(70,3,CpfIso8583FieldSpec.Format.FIXED,true));
    var iso=new CpfIso8583Codec(specs,StandardCharsets.US_ASCII);var msg=new CpfIso8583Message("0800",Map.of(2,"1234567890123456",3,"000000",70,"301"));var wire=iso.encode(msg);var back=iso.decode(wire);check(back.fields().equals(msg.fields()),"ISO secondary bitmap roundtrip");
    var mac=new CpfIso8583HmacMacProvider("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8),16);byte[] tag=mac.mac(wire);check(mac.verify(wire,tag),"ISO MAC verify");wire[wire.length-1]^=1;check(!mac.verify(wire,tag),"ISO MAC tamper");

    // TCP framing + fault/reconcile/correlation/backoff
    byte[] hello="HELLO".getBytes(StandardCharsets.UTF_8);for(var f:CpfTcpProperties.Frame.values())check(Arrays.equals(frame(f,hello,5),hello),"TCP frame "+f);
    CpfTcpProperties tp=new CpfTcpProperties();tp.setEnabled(true);tp.setPort(1);tp.setFrame(CpfTcpProperties.Frame.LENGTH_HEADER);tp.setMaxFrameBytes(4);var tc=new CpfTcpFrameCodec(tp);expect(UncheckedIOException.class,()->{try{tc.write(new ByteArrayOutputStream(),hello);}catch(IOException e){throw new UncheckedIOException(e);}},"TCP oversize");
    CpfTcpProperties simP=new CpfTcpProperties();simP.setEnabled(true);simP.setPort(1);simP.setFrame(CpfTcpProperties.Frame.LENGTH_HEADER);simP.setMaxFrameBytes(1024);var simCodec=new CpfTcpFrameCodec(simP);ByteArrayOutputStream req=new ByteArrayOutputStream();simCodec.write(req,hello);var sim=new CpfTcpDeterministicSimulator(simP,b->b);expect(UncheckedIOException.class,()->{try{sim.exchange(req.toByteArray(),CpfTcpDeterministicSimulator.Fault.DROP_AFTER_READ);}catch(IOException e){throw new UncheckedIOException(e);}},"TCP response loss");check(sim.requestCount()==1,"simulator request count");
    var unknown=new CpfTcpUnknownResultStore(2);unknown.record(new CpfTcpUnknownResult("c1",Instant.EPOCH,hello,"timeout"));check(unknown.find("c1").isPresent(),"unknown stored");check(unknown.reconcile("c1"),"unknown reconciled");check(unknown.find("c1").isEmpty(),"unknown removed");
    var corr=new CpfTcpCorrelationRegistry(2,2);Clock c0=Clock.fixed(Instant.parse("2026-08-02T00:00:00Z"),ZoneOffset.UTC);CompletableFuture<byte[]> future=corr.register("x",Duration.ofSeconds(1),c0);check(corr.complete("x",hello,c0),"correlation complete");check(Arrays.equals(future.join(),hello),"correlation value");check(!corr.complete("orphan",hello,c0),"orphan captured");check(corr.orphans().size()==1,"orphan count");
    var rp=new CpfTcpReconnectPolicy(Duration.ofMillis(100),Duration.ofSeconds(2),0.2);check(rp.delay(3,7).toMillis()>=640&&rp.delay(3,7).toMillis()<=960,"reconnect jitter range");
    CpfTcpProperties tlsP=new CpfTcpProperties();tlsP.setEnabled(true);tlsP.setPort(1);tlsP.setTls(true);expect(IllegalStateException.class,tlsP::validate,"TLS missing stores fail closed");

    // Archive zip/unzip limits and traversal
    Path temp=Files.createTempDirectory("cpf-qa38-archive");Path source=temp.resolve("a.txt");Files.writeString(source,"hello");CpfArchiveProperties ap=new CpfArchiveProperties();ap.setMaxEntries(10);ap.setMaxExpandedBytes(1024);ap.setMaxExpansionRatio(100);var archive=new CpfArchiveService(ap);Path zip=temp.resolve("a.zip");archive.zip(zip,List.of(source),temp);Path out=temp.resolve("out");var extracted=archive.unzip(zip,out);check(Files.readString(extracted.getFirst()).equals("hello"),"archive roundtrip");
    Path evil=temp.resolve("evil.zip");try(var z=new ZipOutputStream(Files.newOutputStream(evil))){z.putNextEntry(new ZipEntry("../evil.txt"));z.write("x".getBytes());z.closeEntry();}expect(UncheckedIOException.class,()->{try{archive.unzip(evil,temp.resolve("evil-out"));}catch(IOException e){throw new UncheckedIOException(e);}},"zip slip blocked");

    // Notification preference and quiet hours
    var policy=new CpfNotificationPreferencePolicy();policy.replace("r",new CpfNotificationPreferencePolicy.Preference(Set.of("EMAIL"),ZoneOffset.UTC,LocalTime.of(22,0),LocalTime.of(7,0)));var nr=new CpfNotificationRequest("n","EMAIL","r","t",Map.of(),"i","tx",null);var decision=policy.evaluate(nr,Clock.fixed(Instant.parse("2026-08-02T23:00:00Z"),ZoneOffset.UTC));check(!decision.allowed()&&decision.reason().equals("QUIET_HOURS")&&decision.resumeAt().equals(Instant.parse("2026-08-03T07:00:00Z")),"quiet hours overnight");

    // Service identity signing, audience and rotation
    var sp=new CpfServiceIdentityProperties();sp.setEnabled(true);sp.setServiceId("svc-a");sp.setActiveKeyId("k2");sp.setActiveSecret("0123456789abcdef0123456789abcdef");sp.setPreviousKeyId("k1");sp.setPreviousSecret("abcdef0123456789abcdef0123456789");sp.setTtl(Duration.ofMinutes(2));Clock sc=Clock.fixed(Instant.parse("2026-08-02T00:00:00Z"),ZoneOffset.UTC);var tokens=new CpfServiceIdentityTokenService(sp,sc);String token=tokens.issue("svc-b","nonce");var vi=tokens.verify(token,"svc-b");check(vi.serviceId().equals("svc-a")&&vi.nonce().equals("nonce"),"service identity verify");expect(SecurityException.class,()->tokens.verify(token,"svc-c"),"service identity audience");

    // Named provider binding atomic fail-closed behavior
    var reg=new CpfCapabilityBindingRegistry();reg.register(new CpfCapabilityBinding("messaging","kafka","kafka",true,Map.of()));expect(IllegalStateException.class,()->reg.register(new CpfCapabilityBinding("messaging","rabbit","rabbitmq",true,Map.of())),"multiple default fail closed");check(reg.list("messaging").size()==1&&reg.requireDefault("messaging").name().equals("kafka"),"failed registration rolled back");

    System.out.println("QA38 PURE RUNTIME HARNESS PASS checks="+checks);
  }
}
