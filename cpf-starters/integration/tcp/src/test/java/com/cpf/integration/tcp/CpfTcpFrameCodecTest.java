package com.cpf.integration.tcp;
import java.io.*;import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.*;
class CpfTcpFrameCodecTest {
 private static CpfTcpProperties props(CpfTcpProperties.Frame frame){var p=new CpfTcpProperties();p.setEnabled(true);p.setPort(10000);p.setFrame(frame);p.setMaxFrameBytes(64);if(frame==CpfTcpProperties.Frame.FIXED)p.setFixedLength(3);p.validate();return p;}
 @Test void roundTripsAllFramingModes()throws Exception{for(var frame:CpfTcpProperties.Frame.values()){var codec=new CpfTcpFrameCodec(props(frame));byte[] value=frame==CpfTcpProperties.Frame.FIXED?new byte[]{1,2,3}:new byte[]{1,2,3,4};var out=new ByteArrayOutputStream();codec.write(out,value);assertThat(codec.read(new ByteArrayInputStream(out.toByteArray()))).containsExactly(value);}}
 @Test void rejectsOversizeAndMalformedLength()throws Exception{var codec=new CpfTcpFrameCodec(props(CpfTcpProperties.Frame.LENGTH_HEADER));assertThatThrownBy(()->codec.read(new ByteArrayInputStream(new byte[]{0,0,1,0}))).isInstanceOf(IOException.class);}
 @Test void unknownResultStoreRequiresExplicitReconcile(){var store=new CpfTcpUnknownResultStore(1);store.record(new CpfTcpUnknownResult("c1",java.time.Instant.EPOCH,new byte[]{1},"lost"));assertThat(store.find("c1")).isPresent();assertThat(store.reconcile("c1")).isTrue();assertThat(store.find("c1")).isEmpty();}
}
