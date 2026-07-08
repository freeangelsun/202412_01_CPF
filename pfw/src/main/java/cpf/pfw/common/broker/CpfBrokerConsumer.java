package cpf.pfw.common.broker;

/**
 * PFW broker 수신 port입니다.
 */
public interface CpfBrokerConsumer {

    CpfBrokerResult consume(CpfBrokerEnvelope envelope);
}
