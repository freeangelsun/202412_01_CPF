package cpf.pfw.common.remotelog;

/** 원격 인스턴스 호출용 단기 service token을 secret provider와 분리하는 port입니다. */
public interface CpfRemoteLogServiceCredentialPort {

    String issueServiceToken(CpfRemoteLogNode node);
}
