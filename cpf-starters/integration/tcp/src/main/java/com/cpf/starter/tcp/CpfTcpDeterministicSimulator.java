package com.cpf.starter.tcp;
import java.io.*;import java.util.concurrent.atomic.AtomicInteger;import java.util.function.Function;
/** Deterministic protocol simulator used by contract and fault tests without external infrastructure. */
public final class CpfTcpDeterministicSimulator {
 public enum Fault { NONE, FRAGMENT_RESPONSE, DROP_AFTER_READ, MALFORMED_LENGTH }
 private final CpfTcpFrameCodec codec;private final Function<byte[],byte[]> handler;private final AtomicInteger requests=new AtomicInteger();
 public CpfTcpDeterministicSimulator(CpfTcpProperties p,Function<byte[],byte[]> handler){this.codec=new CpfTcpFrameCodec(p);this.handler=handler;}
 public byte[] exchange(byte[] framedRequest,Fault fault)throws IOException{ByteArrayInputStream in=new ByteArrayInputStream(framedRequest);byte[] request=codec.read(in);requests.incrementAndGet();if(fault==Fault.DROP_AFTER_READ)throw new EOFException("simulated response loss");byte[] response=handler.apply(request);ByteArrayOutputStream out=new ByteArrayOutputStream();if(fault==Fault.MALFORMED_LENGTH){out.write(new byte[]{0x7f,(byte)0xff,(byte)0xff,(byte)0xff});return out.toByteArray();}codec.write(out,response);byte[] bytes=out.toByteArray();if(fault==Fault.FRAGMENT_RESPONSE){ByteArrayOutputStream fragments=new ByteArrayOutputStream();for(byte b:bytes)fragments.write(b);return fragments.toByteArray();}return bytes;}
 public int requestCount(){return requests.get();}
}
