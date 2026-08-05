package com.cpf.core.common.logging.file;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.core.env.Environment;
public final class CpfFileLogBoundaryHarness {
 private CpfFileLogBoundaryHarness(){}
 public static void main(String[] args)throws Exception{
  MutableEnvironment env=new MutableEnvironment();
  CpfFileLogWriter writer=new CpfFileLogWriter(env,Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"),ZoneOffset.UTC));
  Method parse=CpfFileLogWriter.class.getDeclaredMethod("parseSize",String.class); parse.setAccessible(true);
  boolean negative=false; try{parse.invoke(writer,"-1GB");}catch(InvocationTargetException e){negative=e.getCause() instanceof IllegalArgumentException;}
  check(negative,"negative size cap is rejected instead of disabling retention");
  Method extract=CpfFileLogWriter.class.getDeclaredMethod("extractLogDate",Path.class); extract.setAccessible(true);
  check(extract.invoke(writer,Path.of("app-2026-99-99.log"))==null,"invalid filename date falls back safely");
  Method permissions=CpfFileLogWriter.class.getDeclaredMethod("applyPosixPermissions",Path.class,String.class,boolean.class); permissions.setAccessible(true);
  Path file=Files.createTempFile("cpf-perm-",".log");
  boolean invalidPermission=false; try{permissions.invoke(writer,file,"not-a-mode",false);}catch(InvocationTargetException e){invalidPermission=e.getCause() instanceof java.io.IOException;}
  check(invalidPermission,"permission syntax validated independently of OS support");
  env.interval=Long.MAX_VALUE;
  Method retention=CpfFileLogWriter.class.getDeclaredMethod("shouldRunRetention"); retention.setAccessible(true);
  boolean excessiveInterval=false; try{retention.invoke(writer);}catch(InvocationTargetException e){excessiveInterval=e.getCause() instanceof IllegalArgumentException;}
  check(excessiveInterval,"retention interval has a resource safety bound");
  MutableClock mutableClock=new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
  MutableEnvironment scheduleEnv=new MutableEnvironment(); scheduleEnv.interval=1000L;
  CpfFileLogWriter scheduled=new CpfFileLogWriter(scheduleEnv,mutableClock);
  check(Boolean.TRUE.equals(retention.invoke(scheduled)),"first retention schedule is admitted");
  check(Boolean.FALSE.equals(retention.invoke(scheduled)),"retention is throttled before interval");
  mutableClock.advance(Duration.ofDays(1));
  check(Boolean.TRUE.equals(retention.invoke(scheduled)),"retention resumes after time advances across log dates");
  Field scheduler=CpfFileLogWriter.class.getDeclaredField("nextRetentionCheckEpochMillis"); scheduler.setAccessible(true);
  check(scheduler.get(scheduled) instanceof AtomicLong,"retention scheduler uses one bounded slot per writer");
  System.out.println("CPF_FILE_LOG_BOUNDARY_HARNESS_PASS");
 }
 private static void check(boolean c,String m){if(!c)throw new AssertionError(m);}
 private static final class MutableClock extends Clock{
  private Instant instant; MutableClock(Instant instant){this.instant=instant;}
  void advance(Duration duration){instant=instant.plus(duration);}
  @Override public ZoneId getZone(){return ZoneOffset.UTC;}
  @Override public Clock withZone(ZoneId zone){return this;}
  @Override public Instant instant(){return instant;}
 }
 private static final class MutableEnvironment implements Environment{
  long interval=60000L;
  @Override public String getProperty(String key){return switch(key){case "cpf.logging.file.enabled"->"false";case "cpf.logging.file.timezone"->"UTC";default->null;};}
  @SuppressWarnings("unchecked") @Override public <T>T getProperty(String key,Class<T> type,T d){
   if("cpf.logging.file.enabled".equals(key)&&type==Boolean.class)return (T)Boolean.FALSE;
   if("cpf.logging.file.retention-check-interval-ms".equals(key)&&type==Long.class)return (T)Long.valueOf(interval);
   return d;
  }
  @Override public String[]getActiveProfiles(){return new String[0];}
 }
}
