package cpf.acc.bse.service;

import cpf.acc.bse.dto.MbrMemberDetailResponse;
import cpf.pfw.common.http.CpfWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@Service
@RequiredArgsConstructor
public class MbrMemberClientService {

    /** application-pfw.yml??cpf.services.mbr 한글 한글 한글 ?⑸땲?? */
    private static final String MBR_SERVICE_ID = "mbr";

    private final CpfWebClient cpfWebClient;

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
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
