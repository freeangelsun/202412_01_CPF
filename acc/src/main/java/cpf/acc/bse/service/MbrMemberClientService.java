package cpf.acc.bse.service;

import cpf.acc.bse.dto.MbrMemberDetailResponse;
import cpf.pfw.common.http.FpsWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ACC 二쇱젣?곸뿭?먯꽌 MBR 二쇱젣?곸뿭 API瑜??몄텧?섎뒗 ?섑뵆 ?쒕퉬?ㅼ엯?덈떎.
 * FpsWebClient媛 嫄곕옒ID, TraceId, 梨꾨꼸, ?뚯썝踰덊샇 ???쒖? ?ㅻ뜑瑜??먮룞?쇰줈 ?꾪뙆?⑸땲??
 */
@Service
@RequiredArgsConstructor
public class MbrMemberClientService {

    /** application-pfw.yml??cpf.services.mbr ?ㅼ젙 ?ㅼ? ?쇱튂?댁빞 ?⑸땲?? */
    private static final String MBR_SERVICE_ID = "mbr";

    private final FpsWebClient fpsWebClient;

    /**
     * MBR ?뚯썝 ?곸꽭議고쉶 API瑜??몄텧?⑸땲??
     *
     * @param memberId 議고쉶???뚯썝 ID
     * @return MBR ?뚯썝 ?곸꽭議고쉶 ?묐떟
     */
    public MbrMemberDetailResponse getMemberDetail(Integer memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("?뚯썝 ID???묒닔?ъ빞 ?⑸땲??");
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

