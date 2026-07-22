package com.cpf.common.mqe.config;

import com.cpf.common.mqe.core.CmnMessageBrokerType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** CMN 메시징 브로커와 기본 목적지 설정을 바인딩합니다. */
@ConfigurationProperties(prefix = "cpf.cmn.messaging")
public class CmnMessagingProperties {

    /** 공통 메시징 기능의 활성화 여부입니다. */
    private boolean enabled = true;

    /** 사용할 브로커 구현이며 로컬 기본값은 인메모리입니다. */
    private CmnMessageBrokerType broker = CmnMessageBrokerType.IN_MEMORY;

    /** 발행 요청에 목적지가 없을 때 사용할 기본 목적지입니다. */
    private String defaultDestination = "cpf.default.event";

    /** 운영 조회를 위해 메모리에 보관할 최근 메시지 최대 건수입니다. */
    private int recentMessageLimit = 200;

    /** RabbitMQ 전용 연결 논리 설정입니다. */
    private Rabbit rabbit = new Rabbit();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CmnMessageBrokerType getBroker() {
        return broker;
    }

    public void setBroker(CmnMessageBrokerType broker) {
        this.broker = broker;
    }

    public String getDefaultDestination() {
        return defaultDestination;
    }

    public void setDefaultDestination(String defaultDestination) {
        this.defaultDestination = defaultDestination;
    }

    public int getRecentMessageLimit() {
        return recentMessageLimit;
    }

    public void setRecentMessageLimit(int recentMessageLimit) {
        this.recentMessageLimit = recentMessageLimit;
    }

    public Rabbit getRabbit() {
        return rabbit;
    }

    public void setRabbit(Rabbit rabbit) {
        this.rabbit = rabbit;
    }

    /** RabbitMQ 전용 exchange와 routing key 설정입니다. */
    public static class Rabbit {
        /** 메시지를 발행할 exchange 이름입니다. */
        private String exchange = "cpf.exchange";

        /** 기본 routing key입니다. */
        private String routingKey = "cpf.default.event";

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }
    }
}
