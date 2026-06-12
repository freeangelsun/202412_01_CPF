package fps.acc.bse.service;

import fps.acc.bse.dto.MbrMemberDetailResponse;
import fps.pfw.common.http.FpsWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ACC 주제영역에서 MBR 주제영역 API를 호출하는 샘플 서비스입니다.
 * FpsWebClient가 거래ID, TraceId, 채널, 회원번호 등 표준 헤더를 자동으로 전파합니다.
 */
@Service
@RequiredArgsConstructor
public class MbrMemberClientService {

    /** application-pfw.yml의 fps.services.mbr 설정 키와 일치해야 합니다. */
    private static final String MBR_SERVICE_ID = "mbr";

    private final FpsWebClient fpsWebClient;

    /**
     * MBR 회원 상세조회 API를 호출합니다.
     *
     * @param memberId 조회할 회원 ID
     * @return MBR 회원 상세조회 응답
     */
    public MbrMemberDetailResponse getMemberDetail(Integer memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("회원 ID는 양수여야 합니다.");
        }

        return fpsWebClient.get(
                MBR_SERVICE_ID,
                uriBuilder -> uriBuilder
                        .path("/mbr/detail")
                        .queryParam("memberId", memberId)
                        .build(),
                MbrMemberDetailResponse.class);
    }
}
