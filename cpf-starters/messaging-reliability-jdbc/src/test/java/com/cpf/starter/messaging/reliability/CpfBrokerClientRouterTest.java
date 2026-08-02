package com.cpf.starter.messaging.reliability;
import com.cpf.core.api.broker.*;import java.time.Instant;import java.util.*;import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.*;
class CpfBrokerClientRouterTest {
 private static CpfBrokerClient client(String provider){return r->new CpfBrokerPublishResult("PUBLISHED",r.messageId(),provider,r.destination(),Instant.EPOCH,"ok");}
 @Test void routesDefaultAndNamedProvider(){var router=new CpfBrokerClientRouter(List.of(new CpfNamedBrokerClient("primary","KAFKA",true,client("KAFKA")),new CpfNamedBrokerClient("migration","RABBITMQ",false,client("RABBITMQ"))));var request=new CpfBrokerPublishRequest("m1","orders","k",new byte[]{1},Map.of(),"tx1","idem1");assertThat(router.enqueue(request).provider()).isEqualTo("KAFKA");assertThat(router.enqueue("migration",request).provider()).isEqualTo("RABBITMQ");}
 @Test void failsClosedOnAmbiguousDefaults(){assertThatThrownBy(()->new CpfBrokerClientRouter(List.of(new CpfNamedBrokerClient("a","KAFKA",false,client("KAFKA")),new CpfNamedBrokerClient("b","RABBITMQ",false,client("RABBITMQ"))))).isInstanceOf(IllegalStateException.class);}
}
