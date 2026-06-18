package cpf.acc.bse.service;

import cpf.acc.bse.dto.MbrMemberDetailResponse;
import cpf.pfw.common.http.CpfWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}

