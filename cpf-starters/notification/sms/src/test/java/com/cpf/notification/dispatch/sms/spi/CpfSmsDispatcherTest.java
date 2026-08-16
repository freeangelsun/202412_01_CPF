package com.cpf.notification.dispatch.sms.spi;import java.util.concurrent.atomic.AtomicInteger;import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.*;
class CpfSmsDispatcherTest {
 @Test void deduplicatesByIdempotencyKey(){var calls=new AtomicInteger();CpfSmsProvider p=new CpfSmsProvider(){public String providerName(){return "test";}public SubmitResult submit(String r,String t,String k){calls.incrementAndGet();return new SubmitResult("ACCEPTED","p1",null);}};var d=new CpfSmsDispatcher(p);d.send("010","hello","k1");d.send("010","hello","k1");assertThat(calls).hasValue(1);}
 @Test void mapsTimeoutToUnknownResult(){CpfSmsProvider p=new CpfSmsProvider(){public String providerName(){return "test";}public SubmitResult submit(String r,String t,String k){throw new IllegalStateException("timeout");}};assertThat(new CpfSmsDispatcher(p).send("010","hello","k1").status()).isEqualTo("UNKNOWN_RESULT");}
}
