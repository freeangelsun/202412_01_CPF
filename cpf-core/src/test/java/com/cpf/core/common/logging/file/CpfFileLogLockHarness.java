package com.cpf.core.common.logging.file;
import java.lang.reflect.Method;import java.nio.file.Path;import java.time.Clock;import java.util.ArrayList;import java.util.List;import java.util.concurrent.*;import org.springframework.core.env.Environment;
public final class CpfFileLogLockHarness {
 public static void main(String[] args)throws Exception{
  Environment env=new Environment(){public String getProperty(String k){return "cpf.logging.file.enabled".equals(k)?"false":null;} @SuppressWarnings("unchecked") public <T>T getProperty(String k,Class<T>t,T d){if("cpf.logging.file.enabled".equals(k)&&t==Boolean.class)return (T)Boolean.FALSE;return d;} public String[]getActiveProfiles(){return new String[0];}};
  CpfFileLogWriter writer=new CpfFileLogWriter(env,Clock.systemUTC());
  Method acquire=CpfFileLogWriter.class.getDeclaredMethod("acquireFileLock",Path.class);acquire.setAccessible(true);
  Class<?> entry=Class.forName("com.cpf.core.common.logging.file.CpfFileLogWriter$FileLockEntry");
  Method release=CpfFileLogWriter.class.getDeclaredMethod("releaseFileLock",Path.class,entry);release.setAccessible(true);
  Method count=CpfFileLogWriter.class.getDeclaredMethod("retainedLockEntryCount");count.setAccessible(true);
  Method key=CpfFileLogWriter.class.getDeclaredMethod("logicalLockKey",Path.class);key.setAccessible(true);
  for(int i=0;i<2000;i++){Path p=Path.of("/tmp/log-"+i+".log");Object e=acquire.invoke(writer,p);release.invoke(writer,p,e);}
  if(((Integer)count.invoke(writer))!=0)throw new AssertionError("lock registry leak");
  Object a=key.invoke(writer,Path.of("/tmp/a.log"));Object b=key.invoke(writer,Path.of("/tmp/a.log.gz"));if(!a.equals(b))throw new AssertionError("gzip logical lock mismatch");
  ExecutorService pool=Executors.newFixedThreadPool(12);List<Future<?>>fs=new ArrayList<>();for(int i=0;i<100;i++)fs.add(pool.submit(()->{try{Path p=Path.of("/tmp/shared.log");Object e=acquire.invoke(writer,p);try{Thread.sleep(1);}finally{release.invoke(writer,p,e);}}catch(Exception x){throw new RuntimeException(x);}}));for(Future<?>f:fs)f.get(10,TimeUnit.SECONDS);pool.shutdownNow();if(((Integer)count.invoke(writer))!=0)throw new AssertionError("concurrent registry leak");
  System.out.println("CPF_FILE_LOG_LOCK_HARNESS_PASS");
 }
}
