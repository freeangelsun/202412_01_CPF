package cpf.cmn.mqe.config;

import cpf.cmn.mqe.core.CmnMessageBrokerType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CMN 硫붿떆吏 ??怨듯넻 紐⑤뱢 ?ㅼ젙?낅땲??
 *
 * <p>?낅Т 媛쒕컻?먮뒗 Kafka, RabbitMQ, 濡쒖뺄 ?몃찓紐⑤━ 以??대뼡 釉뚮줈而ㅻ? ?ъ슜?좎?
 * {@code cpf.cmn.messaging.broker} ?ㅼ젙留?諛붽씀硫??⑸땲?? 濡쒖뺄 援먯쑁 ?섍꼍?먯꽌?? * 蹂꾨룄 釉뚮줈而??놁씠 ?숈옉?섎룄濡?湲곕낯媛믪쓣 {@code IN_MEMORY}濡??〓땲??</p>
 */
@ConfigurationProperties(prefix = "cpf.cmn.messaging")
public class CmnMessagingProperties {

    /**
     * 硫붿떆吏?湲곕뒫 ?꾩껜 ?ъ슜 ?щ??낅땲??
     */
    private boolean enabled = true;

    /**
     * ?ъ슜??釉뚮줈而?醫낅쪟?낅땲?? ?댁쁺? KAFKA ?먮뒗 RABBIT, 濡쒖뺄 援먯쑁? IN_MEMORY瑜?沅뚯옣?⑸땲??
     */
    private CmnMessageBrokerType broker = CmnMessageBrokerType.IN_MEMORY;

    /**
     * 紐⑹쟻吏媛 ?앸왂?먯쓣 ???ъ슜??湲곕낯 topic/queue ?대쫫?낅땲??
     */
    private String defaultDestination = "cpf.default.event";

    /**
     * 援먯쑁/吏꾨떒?⑹쑝濡?硫붾え由ъ뿉 蹂닿???理쒓렐 硫붿떆吏 媛쒖닔?낅땲??
     */
    private int recentMessageLimit = 200;

    /**
     * RabbitMQ ?ъ슜 ??湲곕낯 exchange? routing key瑜?愿由ы빀?덈떎.
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
     * RabbitMQ ?꾩슜 ?몃? ?ㅼ젙?낅땲??
     */
    public static class Rabbit {
        /**
         * 硫붿떆吏瑜?諛쒗뻾??exchange ?대쫫?낅땲??
         */
        private String exchange = "cpf.exchange";

        /**
         * destination???놁쓣 ???ъ슜??湲곕낯 routing key?낅땲??
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

