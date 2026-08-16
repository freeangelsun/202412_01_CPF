package com.cpf.starter.data.transaction.jta;
import javax.transaction.xa.*; import java.nio.file.*; import java.util.concurrent.atomic.AtomicInteger;
/** XA prepare crash harness 전용 resource wrapper. cpf.xa.harness.crash=true 일 때만 process halt를 수행합니다. */
final class CpfXaCrashProbeResource implements XAResource {
 private final XAResource delegate; private final Path marker; private final int expectedPrepared; private final AtomicInteger prepared;
 CpfXaCrashProbeResource(XAResource delegate,Path marker,int expectedPrepared,AtomicInteger prepared){this.delegate=delegate;this.marker=marker;this.expectedPrepared=expectedPrepared;this.prepared=prepared;}
 @Override public int prepare(Xid xid)throws XAException{int result=delegate.prepare(xid);if(result==XA_OK&&prepared.incrementAndGet()>=expectedPrepared){try{Files.writeString(marker,"PREPARED");}catch(Exception e){throw new XAException(e.toString());}if(Boolean.getBoolean("cpf.xa.harness.crash")){Thread.startVirtualThread(()->{try{Thread.sleep(25);}catch(InterruptedException ignored){}Runtime.getRuntime().halt(73);});}}return result;}
 @Override public void commit(Xid x,boolean o)throws XAException{delegate.commit(x,o);}@Override public void end(Xid x,int f)throws XAException{delegate.end(x,f);}@Override public void forget(Xid x)throws XAException{delegate.forget(x);}@Override public int getTransactionTimeout()throws XAException{return delegate.getTransactionTimeout();}@Override public boolean isSameRM(XAResource x)throws XAException{return delegate.isSameRM(x);}@Override public Xid[] recover(int f)throws XAException{return delegate.recover(f);}
 @Override public void rollback(Xid x)throws XAException{delegate.rollback(x);}@Override public boolean setTransactionTimeout(int s)throws XAException{return delegate.setTransactionTimeout(s);}@Override public void start(Xid x,int f)throws XAException{delegate.start(x,f);}
}
