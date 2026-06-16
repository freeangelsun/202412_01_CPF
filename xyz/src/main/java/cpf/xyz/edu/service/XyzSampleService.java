package cpf.xyz.edu.service;

import cpf.cmn.utils.DateTimeUtils;
import cpf.cmn.utils.IdUtils;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.FpsBusinessException;
import cpf.pfw.common.exception.FpsNotFoundException;
import cpf.pfw.common.exception.FpsValidationException;
import cpf.xyz.edu.dto.XyzSampleRequest;
import cpf.xyz.edu.dto.XyzSampleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * XYZ 援먯쑁???섑뵆 ?낅Т ?쒕퉬?ㅼ엯?덈떎.
 *
 * <p>?ㅼ젣 DB Mapper ???硫붾え由???μ냼瑜??ъ슜???좉퇋 媛쒕컻?먭? 而⑦듃濡ㅻ윭, ?쒕퉬??
 * ?쒖? ?덉쇅, ?몃옖??뀡 ?좎뼵 ?먮쫫??遺???놁씠 ?뺤씤?????덇쾶 ?⑸땲??</p>
 */
@Service
public class XyzSampleService {
    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentMap<Long, XyzSampleResponse> samples = new ConcurrentHashMap<>();
    private final XyzAuditSampleService auditSampleService;

    public XyzSampleService(XyzAuditSampleService auditSampleService) {
        this.auditSampleService = auditSampleService;
        createSeedData();
    }

    /**
     * 紐⑸줉 議고쉶 ?섑뵆?낅땲??
     *
     * <p>readOnly ?몃옖??뀡 ?좎뼵? 議고쉶 ?꾩슜 ?섎룄瑜?紐낇솗??蹂댁뿬二쇨린 ?꾪븳 ?덉떆?낅땲??</p>
     *
     * @return ?섑뵆 紐⑸줉
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public List<XyzSampleResponse> findSamples() {
        return samples.values().stream()
                .sorted(Comparator.comparing(XyzSampleResponse::sampleId))
                .toList();
    }

    /**
     * ?④굔 議고쉶 ?섑뵆?낅땲??
     *
     * @param sampleId ?섑뵆 ID
     * @return ?섑뵆 ?묐떟
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public XyzSampleResponse getSample(Long sampleId) {
        if (sampleId == null || sampleId <= 0) {
            throw new FpsValidationException("sampleId???묒닔?ъ빞 ?⑸땲?? sampleId=" + sampleId);
        }
        XyzSampleResponse response = samples.get(sampleId);
        if (response == null) {
            throw new FpsNotFoundException("XYZ ?섑뵆 ?곗씠?곌? ?놁뒿?덈떎. sampleId=" + sampleId);
        }
        return response;
    }

    /**
     * ?깅줉 ?섑뵆?낅땲??
     *
     * @param request ?깅줉 ?붿껌
     * @return ?깅줉???섑뵆
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public XyzSampleResponse createSample(XyzSampleRequest request) {
        String title = TextUtils.requireText(request.title(), "title");
        Long sampleId = sequence.incrementAndGet();
        XyzSampleResponse response = new XyzSampleResponse(
                sampleId,
                title,
                "CREATED",
                TextUtils.defaultIfBlank(request.description(), "XYZ 援먯쑁???섑뵆"),
                DateTimeUtils.nowDateTimeMillis());
        samples.put(sampleId, response);
        return response;
    }

    /**
     * ?섏젙 ?섑뵆?낅땲??
     *
     * <p>?ㅼ젣 ?낅Т?먯꽌???섏젙 ???꾩옱 ?곹깭 寃利? 沅뚰븳 寃利? 蹂寃??대젰 ??μ쓣 ?④퍡 怨좊젮?⑸땲??
     * 援먯쑁???섑뵆?먯꽌??硫붾え由???μ냼??媛믪쓣 援먯껜?섎뒗 諛⑹떇?쇰줈 CRUD ?먮쫫留?蹂댁뿬以띾땲??</p>
     *
     * @param sampleId ?섏젙???섑뵆 ID
     * @param request ?섏젙 ?붿껌
     * @return ?섏젙???섑뵆
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public XyzSampleResponse updateSample(Long sampleId, XyzSampleRequest request) {
        XyzSampleResponse current = getSample(sampleId);
        String title = TextUtils.requireText(request.title(), "title");
        XyzSampleResponse response = new XyzSampleResponse(
                current.sampleId(),
                title,
                "UPDATED",
                TextUtils.defaultIfBlank(request.description(), current.description()),
                current.createdAt());
        samples.put(sampleId, response);
        return response;
    }

    /**
     * ??젣 ?섑뵆?낅땲??
     *
     * <p>?ㅼ젣 湲덉쑖 ?낅Т?먯꽌??臾쇰━ ??젣蹂대떎 ?ъ슜 ?щ? 蹂寃? ?댁? ?곹깭 蹂寃? ??젣 ?대젰 ??μ쓣 ?곗꽑 寃?좏빀?덈떎.
     * 援먯쑁???섑뵆?먯꽌??REST DELETE ?먮쫫怨??쒖? ?덉쇅 泥섎━瑜?蹂댁뿬二쇨린 ?꾪빐 硫붾え由?媛믩쭔 ?쒓굅?⑸땲??</p>
     *
     * @param sampleId ??젣???섑뵆 ID
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public void deleteSample(Long sampleId) {
        getSample(sampleId);
        samples.remove(sampleId);
    }

    /**
     * ?섎굹???몃옖??뀡 ?덉뿉???낅Т 泥섎━? 媛먯궗 泥섎━媛 ?④퍡 ?섑뻾?섎뒗 ?섑뵆?낅땲??
     *
     * @return 泥섎━ 寃곌낵 硫붿떆吏
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public String runSingleTransactionSample() {
        XyzSampleResponse response = createSample(new XyzSampleRequest(
                "SINGLE-" + IdUtils.temporaryId("XYZ"),
                "?섎굹???몃옖??뀡 ?섑뵆",
                "SYSTEM"));
        return "?섎굹???몃옖??뀡?쇰줈 ?섑뵆???깅줉?덉뒿?덈떎. sampleId=" + response.sampleId();
    }

    /**
     * ?먭굅?섏? 媛먯궗 ?대젰??遺꾨━ ?몃옖??뀡?쇰줈 泥섎━?섎뒗 ?섑뵆?낅땲??
     *
     * @param failAfterAudit 媛먯궗 湲곕줉 ???섎룄?곸쑝濡??ㅽ뙣?쒗궗吏 ?щ?
     * @return 泥섎━ 寃곌낵 硫붿떆吏
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public String runSeparatedTransactionSample(boolean failAfterAudit) {
        XyzSampleResponse response = createSample(new XyzSampleRequest(
                "SEPARATED-" + IdUtils.temporaryId("XYZ"),
                "遺꾨━ ?몃옖??뀡 ?섑뵆",
                "SYSTEM"));

        auditSampleService.writeAuditRequiresNew("遺꾨━ ?몃옖??뀡 媛먯궗 ?섑뵆. sampleId=" + response.sampleId());

        if (failAfterAudit) {
            throw new FpsBusinessException("媛먯궗 ?대젰 ??????먭굅???ㅽ뙣瑜?媛?뺥븳 ?섑뵆?낅땲?? sampleId=" + response.sampleId());
        }
        return "遺꾨━ ?몃옖??뀡 ?섑뵆??泥섎━?덉뒿?덈떎. sampleId=" + response.sampleId();
    }

    /**
     * 遺꾨━ ?몃옖??뀡 ?섑뵆?먯꽌 ?④릿 媛먯궗 硫붿떆吏瑜?議고쉶?⑸땲??
     *
     * @return 媛먯궗 硫붿떆吏 紐⑸줉
     */
    public List<String> getAuditMessages() {
        return auditSampleService.getAuditMessages();
    }

    private void createSeedData() {
        List<XyzSampleResponse> seed = new ArrayList<>();
        seed.add(new XyzSampleResponse(1L, "紐⑸줉 議고쉶 ?섑뵆", "READY", "GET /xyz/edu/samples", DateTimeUtils.nowDateTimeMillis()));
        seed.add(new XyzSampleResponse(2L, "?④굔 議고쉶 ?섑뵆", "READY", "GET /xyz/edu/samples/detail", DateTimeUtils.nowDateTimeMillis()));
        seed.forEach(sample -> samples.put(sample.sampleId(), sample));
        sequence.set(2L);
    }
}

