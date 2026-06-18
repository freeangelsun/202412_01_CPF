package cpf.cmn.mqe.config;

import cpf.cmn.mqe.core.CmnMessageBrokerType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@ConfigurationProperties(prefix = "cpf.cmn.messaging")
public class CmnMessagingProperties {

    /**
     * CPF 기능 설명입니다.
     */
    private boolean enabled = true;

    /**
     * CPF 기능 설명입니다.
     */
    private CmnMessageBrokerType broker = CmnMessageBrokerType.IN_MEMORY;

    /**
     * CPF 기능 설명입니다.
     */
    private String defaultDestination = "cpf.default.event";

    /**
     * CPF 기능 설명입니다.
     */
    private int recentMessageLimit = 200;

    /**
     * CPF 기능 설명입니다.
     */
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

    /**
     * CPF 기능 설명입니다.
     */
    public static class Rabbit {
        /**
         * CPF 기능 설명입니다.
         */
        private String exchange = "cpf.exchange";

        /**
         * CPF 기능 설명입니다.
         */
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

