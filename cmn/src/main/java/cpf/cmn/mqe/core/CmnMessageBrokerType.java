package cpf.cmn.mqe.core;

/**
 * CMN 硫붿떆吏?怨듯넻 ⑤뱢?먯꽌 吏?먰븯??釉뚮줈而?醫낅쪟?낅땲??
 */
public enum CmnMessageBrokerType {
    /**
     * 蹂꾨룄 Kafka/RabbitMQ ?놁씠 JVM 硫붾え由ъ뿉?쒕쭔 諛쒗뻾怨??뚮퉬瑜?泥섎━?⑸땲??
     */
    IN_MEMORY,

    /**
     * Spring Kafka??{@code KafkaTemplate}?쇰줈 topic??硫붿떆吏瑜?諛쒗뻾?⑸땲??
     */
    KAFKA,

    /**
     * Spring AMQP??{@code RabbitTemplate}?쇰줈 exchange/routing key??硫붿떆吏瑜?諛쒗뻾?⑸땲??
     */
    RABBIT
}

