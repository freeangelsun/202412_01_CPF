package cpf.acc.tst.controller;

import cpf.cmn.cde.dto.CommonCodeRequest;
import cpf.cmn.cde.service.CodeCacheService;
import cpf.cmn.cfg.dto.CommonConfigRequest;
import cpf.cmn.cfg.service.ConfigCacheService;
import cpf.cmn.msg.dto.CommonMessageRequest;
import cpf.cmn.msg.service.MessageCacheService;
import cpf.pfw.common.logging.FpsTransaction;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ACC?癒?퐣 CMN ?⑤벏???온??疫꿸퀡????紐꾪뀱??롫뮉 ??묐탣 ?뚢뫂?껅에?살쑎??낅빍??
 *
 * <p>CMN?? ?꾨뗀諭? 筌롫뗄?놅쭪?, ??쇱젟揶?揶쏆늿? 揶쏆뮆而??⑤벏???怨쀬뵠?怨? ?온?귐뗫???덈뼄.
 * ???뚢뫂?껅에?살쑎??ACC ??끦??λ퓠??CMN ??뺥돩??? 筌욊낯??雅뚯눘??쳸?녿툡 鈺곌퀬???源낆쨯/??륁젟/?????귐뗫늄??됰뻻???紐꾪뀱??롫뮉
 * ??? ??????됰뻻??낅빍??</p>
 *
 * <p>CRUD揶쎛 ?源껊궗??롢늺 揶?CMN ??뺥돩??? ??? 筌?Ŋ?녺몴?筌앸맩???귐뗫늄??됰뻻???嚥?
 * 揶쏆늿? WAS ??됰퓠??뺣뮉 ??쇱벉 鈺곌퀬?띌겫???筌ㅼ뮇??DB 揶쏅???獄쏆꼷???몃빍??</p>
 */
@RestController
@Validated
@RequestMapping("/acc/cmn")
@Tag(name = "ACC-TST CMN Management", description = "Common code, message, config CRUD and cache refresh sample APIs")
public class CmnManagementController {
    private final CodeCacheService codeCacheService;
    private final MessageCacheService messageCacheService;
    private final ConfigCacheService configCacheService;

    public CmnManagementController(
            CodeCacheService codeCacheService,
            MessageCacheService messageCacheService,
            ConfigCacheService configCacheService) {
        // ?⑤벏???꾨뗀諭?鈺곌퀬??CRUD/筌?Ŋ???귐뗫늄??됰뻻 ??뺥돩??쇱뿯??덈뼄.
        this.codeCacheService = codeCacheService;
        // ?⑤벏??筌롫뗄?놅쭪? 鈺곌퀬??CRUD/筌?Ŋ???귐뗫늄??됰뻻 ??뺥돩??쇱뿯??덈뼄.
        this.messageCacheService = messageCacheService;
        // ?⑤벏????쇱젟揶?鈺곌퀬??CRUD/筌?Ŋ???귐뗫늄??됰뻻 ??뺥돩??쇱뿯??덈뼄.
        this.configCacheService = configCacheService;
    }

    /**
     * ?⑤벏???꾨뗀諭띄몴?鈺곌퀬???몃빍??
     *
     * @param codeId ?꾨뗀諭?ID??낅빍?? ??됱몵筌???ｊ탷 鈺곌퀬?띄몴???묐뻬??몃빍??
     * @param codeKey ?꾨뗀諭???쇱뿯??덈뼄. codeId揶쎛 ??얩?codeKey揶쎛 ??됱몵筌?揶쏆늿? ?꾨뗀諭???筌뤴뫖以??鈺곌퀬???몃빍??
     * @return ?꾨뗀諭???ｊ탷, ?꾨뗀諭???筌뤴뫖以? ?癒?뮉 ?袁⑷퍥 ?꾨뗀諭?筌뤴뫖以??낅빍??
     */
    @GetMapping("/codes")
    @FpsTransaction(id = "ACC09TST0010", name = "?⑤벏?삭굜遺얜굡鈺곌퀬???묐탣")
    public ResponseEntity<Map<String, Object>> getCodes(
            @RequestParam(name = "codeId", required = false) Long codeId,
            @RequestParam(name = "codeKey", required = false) String codeKey) {

        Object data;
        if (codeId != null) {
            // codeId揶쎛 ??됱몵筌?PK 疫꿸퀣? ??ｊ탷 鈺곌퀬?띄몴???묐뻬??몃빍??
            data = codeCacheService.getCodeById(codeId);
        } else if (hasText(codeKey)) {
            // codeKey揶쎛 ??됱몵筌?揶쏆늿? ?꾨뗀諭???쇰퓠 ??곷립 ?꾨뗀諭?筌뤴뫖以??鈺곌퀬???몃빍??
            data = codeCacheService.getCodesByKey(codeKey);
        } else {
            // 鈺곌퀗援????곸몵筌??袁⑷퍥 ?꾨뗀諭?筌뤴뫖以??鈺곌퀬???몃빍??
            data = codeCacheService.getAllCodes();
        }

        return ok("codes fetched", data);
    }

    /**
     * ?⑤벏???꾨뗀諭띄몴??源낆쨯??몃빍??
     *
     * @param request ?源낆쨯???꾨뗀諭??類ｋ궖??낅빍??
     * @return ?源낆쨯???꾨뗀諭??類ｋ궖??낅빍??
     */
    @PostMapping("/codes")
    @FpsTransaction(id = "ACC09TST0011", name = "?⑤벏?삭굜遺얜굡?源낆쨯??묐탣")
    public ResponseEntity<Map<String, Object>> createCode(@Valid @RequestBody CommonCodeRequest request) {
        // ?源낆쨯 ??CodeCacheService揶쎛 ?꾨뗀諭?筌?Ŋ?녺몴?筌앸맩???귐뗫늄??됰뻻??몃빍??
        return ok("code created", codeCacheService.createCode(request));
    }

    /**
     * ?⑤벏???꾨뗀諭띄몴???륁젟??몃빍??
     *
     * @param codeId ??륁젟???꾨뗀諭?ID??낅빍??
     * @param request ??륁젟???꾨뗀諭??類ｋ궖??낅빍??
     * @return ??륁젟???꾨뗀諭??類ｋ궖??낅빍??
     */
    @PutMapping("/codes")
    @FpsTransaction(id = "ACC09TST0012", name = "?⑤벏?삭굜遺얜굡??륁젟??묐탣")
    public ResponseEntity<Map<String, Object>> updateCode(
            @RequestParam("codeId") Long codeId,
            @Valid @RequestBody CommonCodeRequest request) {
        // ??륁젟 ??CodeCacheService揶쎛 ?꾨뗀諭?筌?Ŋ?녺몴?筌앸맩???귐뗫늄??됰뻻??몃빍??
        return ok("code updated", codeCacheService.updateCode(codeId, request));
    }

    /**
     * ?⑤벏???꾨뗀諭띄몴??????몃빍??
     *
     * @param codeId ??????꾨뗀諭?ID??낅빍??
     * @return ??????筌ㅼ뮇???꾨뗀諭?筌뤴뫖以??낅빍??
     */
    @DeleteMapping("/codes")
    @FpsTransaction(id = "ACC09TST0013", name = "?⑤벏?삭굜遺얜굡?????묐탣")
    public ResponseEntity<Map<String, Object>> deleteCode(@RequestParam("codeId") Long codeId) {
        // ??????CodeCacheService揶쎛 ?꾨뗀諭?筌?Ŋ?녺몴?筌앸맩???귐뗫늄??됰뻻??몃빍??
        return ok("code deleted", codeCacheService.deleteCode(codeId));
    }

    /**
     * ?⑤벏???꾨뗀諭?筌?Ŋ?녺몴?筌앸맩???귐뗫늄??됰뻻??몃빍??
     *
     * @return 筌ㅼ뮇???꾨뗀諭?筌뤴뫖以??낅빍??
     */
    @PostMapping("/codes/refresh")
    @FpsTransaction(id = "ACC09TST0014", name = "?⑤벏?삭굜遺얜굡筌?Ŋ?녺뵳?遊??됰뻻??묐탣")
    public ResponseEntity<Map<String, Object>> refreshCodes() {
        // ??곸겫?癒? ?온???遺얇늺?癒?퐣 ??롫짗 ?귐뗫늄??됰뻻 甕곌쑵????袁ⓥ뀮???怨뱀넺??揶쎛?類λ립 ??묐탣??낅빍??
        return ok("code cache refreshed", codeCacheService.refreshCodesAndPublish());
    }

    /**
     * ?⑤벏??筌롫뗄?놅쭪???鈺곌퀬???몃빍??
     *
     * @param messageId 筌롫뗄?놅쭪? ID??낅빍?? ??됱몵筌???ｊ탷 鈺곌퀬?띄몴???묐뻬??몃빍??
     * @param messageKey 筌롫뗄?놅쭪? ??쇱뿯??덈뼄.
     * @param locale ?紐꾨선 ?꾨뗀諭??낅빍?? messageKey?? ??ｍ뜞 ??됱몵筌????紐꾨선 ??ｊ탷 鈺곌퀬?띄몴???묐뻬??몃빍??
     * @param messageType 筌롫뗄?놅쭪? ?醫륁굨??낅빍?? EXTERNAL ?癒?뮉 INTERNAL??낅빍??
     * @return 筌롫뗄?놅쭪? ??ｊ탷 ?癒?뮉 ?袁⑷퍥 筌롫뗄?놅쭪? 筌뤴뫖以??낅빍??
     */
    @GetMapping("/messages")
    @FpsTransaction(id = "ACC09TST0020", name = "?⑤벏?삼쭖遺용뻻筌왖鈺곌퀬???묐탣")
    public ResponseEntity<Map<String, Object>> getMessages(
            @RequestParam(name = "messageId", required = false) Long messageId,
            @RequestParam(name = "messageKey", required = false) String messageKey,
            @RequestParam(name = "locale", required = false) String locale,
            @RequestParam(name = "messageType", required = false) String messageType) {

        Object data;
        if (messageId != null) {
            data = messageCacheService.getMessageById(messageId);
        } else if (hasText(messageKey) && hasText(locale) && hasText(messageType)) {
            // ??살첒 筌롫뗄?놅쭪???EXTERNAL/INTERNAL ?醫륁굨????롫떊 鈺곌퀬???????됰뮸??덈뼄.
            data = messageCacheService.getMessageByKeyLocaleType(messageKey, locale, messageType);
        } else if (hasText(messageKey) && hasText(locale)) {
            data = messageCacheService.getMessageByKeyAndLocale(messageKey, locale);
        } else if (hasText(messageKey)) {
            data = messageCacheService.getMessageByKey(messageKey);
        } else {
            data = messageCacheService.getAllMessages();
        }

        return ok("messages fetched", data);
    }

    /**
     * ?⑤벏??筌롫뗄?놅쭪????源낆쨯??몃빍??
     *
     * @param request ?源낆쨯??筌롫뗄?놅쭪? ?類ｋ궖??낅빍??
     * @return ?源낆쨯??筌롫뗄?놅쭪? ?類ｋ궖??낅빍??
     */
    @PostMapping("/messages")
    @FpsTransaction(id = "ACC09TST0021", name = "?⑤벏?삼쭖遺용뻻筌왖?源낆쨯??묐탣")
    public ResponseEntity<Map<String, Object>> createMessage(@Valid @RequestBody CommonMessageRequest request) {
        return ok("message created", messageCacheService.createMessage(request));
    }

    /**
     * ?⑤벏??筌롫뗄?놅쭪?????륁젟??몃빍??
     *
     * @param messageId ??륁젟??筌롫뗄?놅쭪? ID??낅빍??
     * @param request ??륁젟??筌롫뗄?놅쭪? ?類ｋ궖??낅빍??
     * @return ??륁젟??筌롫뗄?놅쭪? ?類ｋ궖??낅빍??
     */
    @PutMapping("/messages")
    @FpsTransaction(id = "ACC09TST0022", name = "?⑤벏?삼쭖遺용뻻筌왖??륁젟??묐탣")
    public ResponseEntity<Map<String, Object>> updateMessage(
            @RequestParam("messageId") Long messageId,
            @Valid @RequestBody CommonMessageRequest request) {
        return ok("message updated", messageCacheService.updateMessage(messageId, request));
    }

    /**
     * ?⑤벏??筌롫뗄?놅쭪????????몃빍??
     *
     * @param messageId ?????筌롫뗄?놅쭪? ID??낅빍??
     * @return ??????筌ㅼ뮇??筌롫뗄?놅쭪? 筌뤴뫖以??낅빍??
     */
    @DeleteMapping("/messages")
    @FpsTransaction(id = "ACC09TST0023", name = "?⑤벏?삼쭖遺용뻻筌왖?????묐탣")
    public ResponseEntity<Map<String, Object>> deleteMessage(@RequestParam("messageId") Long messageId) {
        return ok("message deleted", messageCacheService.deleteMessage(messageId));
    }

    /**
     * ?⑤벏??筌롫뗄?놅쭪? 筌?Ŋ?녺몴?筌앸맩???귐뗫늄??됰뻻??몃빍??
     *
     * @return 筌ㅼ뮇??筌롫뗄?놅쭪? 筌뤴뫖以??낅빍??
     */
    @PostMapping("/messages/refresh")
    @FpsTransaction(id = "ACC09TST0024", name = "?⑤벏?삼쭖遺용뻻筌왖筌?Ŋ?녺뵳?遊??됰뻻??묐탣")
    public ResponseEntity<Map<String, Object>> refreshMessages() {
        return ok("message cache refreshed", messageCacheService.refreshMessagesAndPublish());
    }

    /**
     * ?⑤벏????쇱젟揶쏅???鈺곌퀬???몃빍??
     *
     * @param configId ??쇱젟 ID??낅빍?? ??됱몵筌???ｊ탷 鈺곌퀬?띄몴???묐뻬??몃빍??
     * @param configKey ??쇱젟 ??쇱뿯??덈뼄. configId揶쎛 ??얩?configKey揶쎛 ??됱몵筌???疫꿸퀣? ??ｊ탷 鈺곌퀬?띄몴???묐뻬??몃빍??
     * @return ??쇱젟揶???ｊ탷 ?癒?뮉 ?袁⑷퍥 ??쇱젟揶?筌뤴뫖以??낅빍??
     */
    @GetMapping("/configs")
    @FpsTransaction(id = "ACC09TST0030", name = "?⑤벏???쇱젟鈺곌퀬???묐탣")
    public ResponseEntity<Map<String, Object>> getConfigs(
            @RequestParam(name = "configId", required = false) Long configId,
            @RequestParam(name = "configKey", required = false) String configKey) {

        Object data;
        if (configId != null) {
            data = configCacheService.getConfigById(configId);
        } else if (hasText(configKey)) {
            data = configCacheService.getConfigByKey(configKey);
        } else {
            data = configCacheService.getAllConfigs();
        }

        return ok("configs fetched", data);
    }

    /**
     * ?⑤벏????쇱젟揶쏅????源낆쨯??몃빍??
     *
     * @param request ?源낆쨯????쇱젟揶??類ｋ궖??낅빍??
     * @return ?源낆쨯????쇱젟揶??類ｋ궖??낅빍??
     */
    @PostMapping("/configs")
    @FpsTransaction(id = "ACC09TST0031", name = "?⑤벏???쇱젟?源낆쨯??묐탣")
    public ResponseEntity<Map<String, Object>> createConfig(@Valid @RequestBody CommonConfigRequest request) {
        return ok("config created", configCacheService.createConfig(request));
    }

    /**
     * ?⑤벏????쇱젟揶쏅?????륁젟??몃빍??
     *
     * @param configId ??륁젟????쇱젟 ID??낅빍??
     * @param request ??륁젟????쇱젟揶??類ｋ궖??낅빍??
     * @return ??륁젟????쇱젟揶??類ｋ궖??낅빍??
     */
    @PutMapping("/configs")
    @FpsTransaction(id = "ACC09TST0032", name = "?⑤벏???쇱젟??륁젟??묐탣")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @RequestParam("configId") Long configId,
            @Valid @RequestBody CommonConfigRequest request) {
        return ok("config updated", configCacheService.updateConfig(configId, request));
    }

    /**
     * ?⑤벏????쇱젟揶쏅????????몃빍??
     *
     * @param configId ???????쇱젟 ID??낅빍??
     * @return ??????筌ㅼ뮇????쇱젟揶?筌뤴뫖以??낅빍??
     */
    @DeleteMapping("/configs")
    @FpsTransaction(id = "ACC09TST0033", name = "?⑤벏???쇱젟?????묐탣")
    public ResponseEntity<Map<String, Object>> deleteConfig(@RequestParam("configId") Long configId) {
        return ok("config deleted", configCacheService.deleteConfig(configId));
    }

    /**
     * ?⑤벏????쇱젟揶?筌?Ŋ?녺몴?筌앸맩???귐뗫늄??됰뻻??몃빍??
     *
     * @return 筌ㅼ뮇????쇱젟揶?筌뤴뫖以??낅빍??
     */
    @PostMapping("/configs/refresh")
    @FpsTransaction(id = "ACC09TST0034", name = "?⑤벏???쇱젟筌?Ŋ?녺뵳?遊??됰뻻??묐탣")
    public ResponseEntity<Map<String, Object>> refreshConfigs() {
        return ok("config cache refreshed", configCacheService.refreshConfigsAndPublish());
    }

    private ResponseEntity<Map<String, Object>> ok(String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

