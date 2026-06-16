package cpf.xyz.edu.controller;

import cpf.pfw.common.http.FpsWebClient;
import cpf.pfw.common.logging.FpsTransaction;
import cpf.pfw.common.workflow.FpsWorkflow;
import cpf.pfw.common.workflow.FpsWorkflowFailurePolicy;
import cpf.pfw.common.workflow.FpsWorkflowStep;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 二쇱젣?곸뿭 媛??몄텧怨??몃? ?뱀궗?댄듃 ?몄텧??蹂댁뿬二쇰뒗 援먯쑁??而⑦듃濡ㅻ윭?낅땲??
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 07. ?쒕퉬???몄텧", description = "PFW WebClient ?ㅻ뜑 ?꾪뙆? ?몃? ?몄텧 ?섑뵆")
public class XyzServiceCallEducationController {
    private final FpsWebClient fpsWebClient;
    private final WebClient externalWebClient;

    public XyzServiceCallEducationController(
            FpsWebClient fpsWebClient,
            WebClient.Builder webClientBuilder) {
        this.fpsWebClient = fpsWebClient;
        this.externalWebClient = webClientBuilder.build();
    }

    /**
     * PFW ?쒖? WebClient濡?MBR ?뚯썝 ?곸꽭瑜??몄텧?⑸땲??
     *
     * @param memberId 議고쉶??MBR ?뚯썝 ID
     * @return MBR ?묐떟
     */
    @GetMapping("/service-call/mbr-detail")
    @FpsTransaction(id = "XYZ08EDU0001", name = "XYZ援먯쑁MBR?곸꽭議고쉶?몄텧")
    @FpsWorkflow(id = "XYZ08EDU9001", name = "XYZ援먯쑁?쒕퉬?ㅽ샇異쒖썙?ы뵆濡쒖슦")
    @FpsWorkflowStep(name = "XYZ?먯꽌MBR?뚯썝?곸꽭議고쉶", failurePolicy = FpsWorkflowFailurePolicy.VERIFY)
    @Operation(summary = "MBR ?쒕퉬???몄텧 ?섑뵆", description = "FpsWebClient媛 嫄곕옒/?뚰겕?뚮줈???ㅻ뜑瑜??먮룞 ?꾪뙆?섎뒗 ?먮쫫???뺤씤?⑸땲??")
    public ResponseEntity<Map<String, Object>> callMbrDetail(@RequestParam Long memberId) {
        Map<String, Object> mbrResponse = fpsWebClient.get(
                "mbr",
                uriBuilder -> uriBuilder
                        .path("/mbr/detail")
                        .queryParam("memberId", memberId)
                        .build(),
                new ParameterizedTypeReference<>() {
                });

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("guide", "FpsWebClient媛 嫄곕옒/?뚰겕?뚮줈???ㅻ뜑瑜??먮룞 ?꾪뙆??MBR ?몄텧 ?섑뵆?낅땲??");
        response.put("mbrResponse", mbrResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * ?몃? ?뱀궗?댄듃瑜?WebClient濡??몄텧?⑸땲??
     *
     * @param url ?몄텧???몃? URL
     * @return ?몃? ?몄텧 ?묐떟
     */
    @GetMapping("/webclient/external-get")
    @FpsTransaction(id = "XYZ08EDU0010", name = "XYZ援먯쑁?몃??뱀궗?댄듃?몄텧")
    @Operation(summary = "?몃? ?뱀궗?댄듃 ?몄텧 ?섑뵆", description = "Spring WebClient濡??몃? ?뚯뒪???ъ씠?몃? ?몄텧?⑸땲??")
    public ResponseEntity<Map<String, Object>> callExternalWebsite(
            @RequestParam(defaultValue = "https://postman-echo.com/get?source=fps-xyz") String url) {
        String responseBody = externalWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(5));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("url", url);
        response.put("body", responseBody);
        response.put("guide", "?쒕퉬???덉??ㅽ듃由???곸? FpsWebClient, ?몃? 怨듦컻 URL? WebClient瑜??ъ슜?⑸땲??");
        return ResponseEntity.ok(response);
    }
}

