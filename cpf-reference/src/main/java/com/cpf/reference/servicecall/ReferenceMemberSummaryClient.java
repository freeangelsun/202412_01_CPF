package cpf.xyz.servicecall;

import cpf.pfw.common.servicecall.CpfTypedServiceClient;

/**
 * XYZ sample이 사용하는 MBR 회원 요약 typed client입니다.
 *
 * <p>업무 코드는 URI, HTTP method, timeout, retry와 transport adapter를 알지 않습니다.</p>
 */
public interface XyzMemberSummaryClient
        extends CpfTypedServiceClient<XyzMemberSummaryRequest, XyzMemberSummaryResponse> {
}
