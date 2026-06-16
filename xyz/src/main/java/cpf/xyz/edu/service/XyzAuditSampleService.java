package cpf.xyz.edu.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 遺꾨━ ?몃옖??뀡 ?덉떆瑜?蹂댁뿬二쇨린 ?꾪븳 媛먯궗 ?섑뵆 ?쒕퉬?ㅼ엯?덈떎.
 *
 * <p>?ㅻТ?먯꽌?????쒕퉬?ㅺ? 媛먯궗 ?뚯씠釉붿뿉 INSERT瑜??섑뻾?⑸땲?? ?꾩옱 XYZ??援먯쑁??紐⑤뱢?대?濡? * 硫붾え由?紐⑸줉??媛먯궗 硫붿떆吏瑜???ν븯?? 硫붿꽌???좎뼵? {@code REQUIRES_NEW}濡??먯뼱
 * 遺꾨━ ?몃옖??뀡???대뼡 ?앹쑝濡??좎뼵?섎뒗吏 蹂댁뿬以띾땲??</p>
 */
@Service
public class XyzAuditSampleService {
    private final List<String> auditMessages = new ArrayList<>();

    /**
     * ?먭굅?섏? 遺꾨━?????몃옖??뀡?쇰줈 媛먯궗 ?대젰???④린???섑뵆?낅땲??
     *
     * @param message 媛먯궗 硫붿떆吏
     */
    @Transactional(transactionManager = "cmnTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void writeAuditRequiresNew(String message) {
        auditMessages.add(message);
    }

    /**
     * ?꾩옱 硫붾え由ъ뿉 ??λ맂 媛먯궗 ?섑뵆 硫붿떆吏瑜?議고쉶?⑸땲??
     *
     * @return 媛먯궗 硫붿떆吏 紐⑸줉
     */
    public List<String> getAuditMessages() {
        return List.copyOf(auditMessages);
    }
}

