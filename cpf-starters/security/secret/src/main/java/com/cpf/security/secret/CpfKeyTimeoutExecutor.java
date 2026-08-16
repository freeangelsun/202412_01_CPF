package com.cpf.security.secret;
import java.time.Duration;import java.util.concurrent.*;import java.util.function.Supplier;
/** Bounded virtual-thread executor for KMS/HSM calls. */
final class CpfKeyTimeoutExecutor implements AutoCloseable {
 private final ExecutorService executor=Executors.newVirtualThreadPerTaskExecutor();private final Duration timeout;
 CpfKeyTimeoutExecutor(Duration timeout){this.timeout=timeout;}
 <T>T call(Supplier<T> work){Future<T> f=executor.submit(work::get);try{return f.get(timeout.toMillis(),TimeUnit.MILLISECONDS);}catch(TimeoutException e){f.cancel(true);throw new IllegalStateException("KEY_PROVIDER_TIMEOUT",e);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("KEY_PROVIDER_INTERRUPTED",e);}catch(ExecutionException e){Throwable c=e.getCause();if(c instanceof RuntimeException r)throw r;throw new IllegalStateException("KEY_PROVIDER_FAILED",c);}}
 @Override public void close(){executor.close();}
}
