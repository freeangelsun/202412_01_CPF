package com.cpf.starter.tcp;
import java.net.*; import java.time.*;
public class ClockHarness { public static void main(String[] a){
  var store=new CpfTcpUnknownResultStore(10); var clock=Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"),ZoneOffset.UTC);
  var x=CpfTcpClient.classifyTransportFailure(true,"c",new byte[]{1},new SocketTimeoutException("x"),store,clock);
  if(!(x instanceof CpfTcpClient.UnknownResultException))throw new AssertionError();
  if(!store.find("c").orElseThrow().writtenAt().equals(clock.instant()))throw new AssertionError();
  System.out.println("S03_TCP_CLOCK_HARNESS PASS cases=2"); }}
