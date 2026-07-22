package com.cpf.common.mqe.core;

/** CMN 메시징이 지원하는 브로커 구현 유형입니다. */
public enum CmnMessageBrokerType {
    /** 외부 인프라 없이 동일 프로세스에서 전달합니다. */
    IN_MEMORY,

    /** Apache Kafka를 통해 비동기 이벤트를 전달합니다. */
    KAFKA,

    /** RabbitMQ exchange와 routing key를 통해 전달합니다. */
    RABBIT
}

