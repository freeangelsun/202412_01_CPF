package cpf.acc.bse.service;

import cpf.acc.bse.dto.MbrMemberDetailResponse;
import cpf.pfw.common.http.CpfWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ACC 모듈에서 MBR 회원 API를 호출하는 표준 outbound 예제 서비스입니다.
 *
 * <p>CPF WebClient가 표준 헤더와 transactionGlobalId를 하위 모듈로 전파하는지 확인하는
 * 교육용 호출 경로로도 사용합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class MbrMemberClientService {

    /** application-pfw.yml의 cpf.services.mbr 설정과 연결되는 서비스 식별자입니다. */
    private static final String MBR_SERVICE_ID = "mbr";

    private final CpfWebClient cpfWebClient;

    /**
     * 회원 상세 조회 API를 호출합니다.
     *
     * <p>memberId 검증은 호출 전에 수행하고, 실제 HTTP 호출은 CPF 공통 WebClient를 사용해
     * 표준 헤더 전파와 거래 구간 로그 저장 흐름을 유지합니다.</p>
     */
    public MbrMemberDetailResponse getMemberDetail(Integer memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("CPF 처리 기준입니다.");
        }

        return cpfWebClient.get(
                MBR_SERVICE_ID,
                uriBuilder -> uriBuilder
                        .path("/mbr/detail")
                        .queryParam("memberId", memberId)
                        .build(),
                MbrMemberDetailResponse.class);
    }

    public Map<String, Object> callCompositeMember(Integer memberId) {
        validateMemberId(memberId);
        return cpfWebClient.get(
                MBR_SERVICE_ID,
                uriBuilder -> uriBuilder
                        .path("/mbr/edu/composite/member-profile")
                        .queryParam("memberId", memberId)
                        .build(),
                new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }

    public Map<String, Object> callMemberWithExternal(Integer memberId) {
        validateMemberId(memberId);
        return cpfWebClient.get(
                MBR_SERVICE_ID,
                uriBuilder -> uriBuilder
                        .path("/mbr/edu/composite/member-calls-external")
                        .queryParam("memberId", memberId)
                        .build(),
                new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }

    private void validateMemberId(Integer memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("memberId는 1 이상의 값이어야 합니다.");
        }
    }
}
