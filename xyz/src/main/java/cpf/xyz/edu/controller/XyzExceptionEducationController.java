package cpf.xyz.edu.controller;

import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.FpsBusinessException;
import cpf.pfw.common.exception.FpsDynamicErrorCode;
import cpf.pfw.common.exception.FpsExternalServiceException;
import cpf.pfw.common.exception.FpsValidationException;
import cpf.pfw.common.logging.FpsTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ?쒖? ?덉쇅? ?숈쟻 ?ㅻ쪟 硫붿떆吏 ?ъ슜踰뺤쓣 蹂댁뿬二쇰뒗 援먯쑁??而⑦듃濡ㅻ윭?낅땲??
 *
 * <p>?낅Т 媛쒕컻?먮뒗 而⑦듃濡ㅻ윭?먯꽌 ?ㅻ쪟 ?묐떟??吏곸젒 留뚮뱾吏 ?딄퀬 PFW ?쒖? ?덉쇅瑜??섏쭛?덈떎.
 * 怨좉컼??硫붿떆吏? ?대? 濡쒓렇 硫붿떆吏??遺꾨━?섍퀬, 硫붿떆吏 ?몄옄??Map?쇰줈 ?꾨떖?⑸땲??</p>
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 03. ?덉쇅/硫붿떆吏", description = "?쒖? ?덉쇅, ?ㅻ쪟肄붾뱶, ?숈쟻 硫붿떆吏 ?섑뵆")
public class XyzExceptionEducationController {

    /**
     * ?쒖? ?덉쇅 ?ъ슜踰뺤쓣 蹂댁뿬以띾땲??
     *
     * @param type ?덉쇅 ?좏삎
     * @return ??硫붿꽌?쒕뒗 ?덉쇅瑜??섏????섑뵆?대?濡??뺤긽 ?묐떟??諛섑솚?섏? ?딆뒿?덈떎.
     */
    @GetMapping("/exception")
    @FpsTransaction(id = "XYZ09EDU0003", name = "XYZ援먯쑁?쒖??덉쇅")
    @Operation(summary = "?쒖? ?덉쇅 ?섑뵆", description = "寃利? ?낅Т, ?몃??곌퀎 ?덉쇅媛 怨듯넻 ?ㅻ쪟 ?묐떟怨?嫄곕옒 濡쒓렇濡?蹂?섎릺???먮쫫???뺤씤?⑸땲??")
    public ResponseEntity<String> throwStandardException(@RequestParam(defaultValue = "validation") String type) {
        String normalizedType = TextUtils.normalizeCode(type);
        if ("BUSINESS".equals(normalizedType)) {
            throw new FpsBusinessException("XYZ ?낅Т 洹쒖튃 ?꾨컲 ?섑뵆?낅땲?? type=" + type);
        }
        if ("EXTERNAL".equals(normalizedType)) {
            throw new FpsExternalServiceException("XYZ ?몃? ?곌퀎 ?ㅽ뙣 ?섑뵆?낅땲??", new IllegalStateException("sample external failure"));
        }
        throw new FpsValidationException("XYZ ?낅젰媛?寃利??ㅽ뙣 ?섑뵆?낅땲?? type=" + type);
    }

    /**
     * ?쒖? ?ㅻ쪟肄붾뱶? ?숈쟻 硫붿떆吏 ?몄옄瑜??④퍡 ?ъ슜?섎뒗 ?섑뵆?낅땲??
     *
     * @param fieldName  ?ㅻ쪟媛 諛쒖깮???낅Т ?꾨뱶紐?     * @param fieldValue ?ㅻ쪟媛 諛쒖깮???낅Т ?꾨뱶媛?     * @return ??硫붿꽌?쒕뒗 ?덉쇅瑜??섏????섑뵆?대?濡??뺤긽 ?묐떟??諛섑솚?섏? ?딆뒿?덈떎.
     */
    @GetMapping("/exception/dynamic-message")
    @FpsTransaction(id = "XYZ09EDU0009", name = "XYZ援먯쑁?숈쟻硫붿떆吏?덉쇅")
    @Operation(summary = "?숈쟻 ?ㅻ쪟肄붾뱶/硫붿떆吏 ?섑뵆", description = "enum 異붽? ?놁씠 ?숈쟻 ?ㅻ쪟?뺤쓽? 硫붿떆吏 ?몄옄濡??ㅻ쪟 ?묐떟怨?濡쒓렇 硫붿떆吏瑜?議곕┰?⑸땲??")
    public ResponseEntity<String> throwDynamicMessageException(
            @RequestParam(defaultValue = "?뚯썝踰덊샇") String fieldName,
            @RequestParam(defaultValue = "M0001") String fieldValue) {

        String externalMessage = "?대? ?깅줉??{fieldName}?낅땲??";
        String internalMessage = "{fieldName}={fieldValue} 媛믪씠 ?대? 議댁옱?⑸땲?? duplicateCheck=XYZ_EDU_SAMPLE";
        Map<String, Object> messageArguments = Map.of(
                "fieldName", fieldName,
                "fieldValue", fieldValue);

        throw new FpsBusinessException(
                FpsDynamicErrorCode.duplicate("MXYZ090001", externalMessage, internalMessage),
                externalMessage,
                internalMessage,
                "XYZ ?숈쟻 硫붿떆吏 議곕┰ ?섑뵆?낅땲??",
                messageArguments);
    }

    @GetMapping("/exception/response-code")
    @FpsTransaction(id = "XYZ09EDU0013", name = "XYZResponseCodeExceptionSample")
    @Operation(summary = "Response code exception sample", description = "Throws only a standard response code. PFW resolves response_code_table and message_table from cache and fills response/log metadata.")
    public ResponseEntity<String> throwResponseCodeException(
            @RequestParam(defaultValue = "EACC010001") String responseCode,
            @RequestParam(defaultValue = "accountId") String fieldName) {

        throw new FpsBusinessException(
                TextUtils.normalizeCode(responseCode),
                "XYZ response-code based exception sample. field=" + fieldName,
                Map.of("0", fieldName));
    }

    @GetMapping("/exception/indexed-message")
    @FpsTransaction(id = "XYZ09EDU0014", name = "XYZIndexedMessageExceptionSample")
    @Operation(summary = "Indexed message exception sample", description = "Demonstrates common indexed placeholders such as {0} and {1}.")
    public ResponseEntity<String> throwIndexedMessageException(
            @RequestParam(defaultValue = "memberNo") String fieldName,
            @RequestParam(defaultValue = "M0001") String fieldValue) {

        throw new FpsBusinessException(
                "EPFW010003",
                "XYZ indexed message exception sample. field=" + fieldName + ", value=" + fieldValue,
                Map.of("0", fieldName, "1", fieldValue));
    }
}

