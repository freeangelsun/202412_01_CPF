package com.cpf.education.online.messaging.listener;
import com.cpf.foundation.annotation.CpfService;import com.cpf.messaging.api.*;import java.util.concurrent.atomic.AtomicReference;
/** 메시징 교육 예제의 Listener 역할과 CPF 표준 사용 경계를 보여줍니다. */
@CpfService public class MemberChangedConsumer {private final AtomicReference<String> lastKey=new AtomicReference<>();@CpfMessageListener(destination="edu.member.changed",consumerGroup="edu-member-consumer") public void consume(CpfBrokerBridgeMessage m){String previous=lastKey.getAndSet(m.key());if(m.key().equals(previous))return;}}
