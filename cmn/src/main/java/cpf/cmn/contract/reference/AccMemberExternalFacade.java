package cpf.cmn.contract.reference;

/**
 * 동일 JVM 배치에서 MBR이 ACC를 직접 구현체 없이 호출하기 위한 Local Facade 계약입니다.
 */
public interface AccMemberExternalFacade {
    AccMemberExternalResponse requestExternal(AccMemberExternalRequest request);
}
