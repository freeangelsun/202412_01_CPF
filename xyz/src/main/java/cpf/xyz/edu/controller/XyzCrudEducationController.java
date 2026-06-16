package cpf.xyz.edu.controller;

import cpf.pfw.common.logging.FpsTransaction;
import cpf.xyz.edu.dto.XyzSampleRequest;
import cpf.xyz.edu.dto.XyzSampleResponse;
import cpf.xyz.edu.service.XyzSampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD ?쒖? 媛쒕컻 ?⑦꽩??蹂댁뿬二쇰뒗 援먯쑁??而⑦듃濡ㅻ윭?낅땲??
 *
 * <p>議고쉶 ?앸퀎?먮뒗 寃쎈줈 蹂?섍? ?꾨땲??紐낆떆 荑쇰━ ?뚮씪誘명꽣濡?諛쏄퀬,
 * ?깅줉/?섏젙 ?곗씠?곕뒗 Body DTO濡?諛쏅뒗 湲덉쑖沅?API ?쒖????섑뵆濡??쒓났?⑸땲??</p>
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 01. CRUD ?쒖?", description = "紐⑸줉/?④굔/?깅줉/?섏젙/??젣 ?쒖? API ?섑뵆")
public class XyzCrudEducationController {
    private final XyzSampleService xyzSampleService;

    public XyzCrudEducationController(XyzSampleService xyzSampleService) {
        this.xyzSampleService = xyzSampleService;
    }

    /**
     * 紐⑸줉 議고쉶 媛쒕컻 ?⑦꽩?낅땲??
     *
     * @return ?섑뵆 紐⑸줉
     */
    @GetMapping("/samples")
    @FpsTransaction(id = "XYZ01EDU0001", name = "XYZ援먯쑁紐⑸줉議고쉶")
    @Operation(summary = "XYZ 援먯쑁 ?섑뵆 紐⑸줉 議고쉶", description = "紐⑸줉 議고쉶, readOnly ?몃옖??뀡, 嫄곕옒 濡쒓렇 ?곸옱 湲곗????뺤씤?⑸땲??")
    public ResponseEntity<List<XyzSampleResponse>> findSamples() {
        return ResponseEntity.ok(xyzSampleService.findSamples());
    }

    /**
     * ?④굔 議고쉶 媛쒕컻 ?⑦꽩?낅땲??
     *
     * @param sampleId ?섑뵆 ID
     * @return ?섑뵆 ?④굔
     */
    @GetMapping("/samples/detail")
    @FpsTransaction(id = "XYZ01EDU0002", name = "XYZ援먯쑁?④굔議고쉶")
    @Operation(summary = "XYZ 援먯쑁 ?섑뵆 ?④굔 議고쉶", description = "紐낆떆 荑쇰━ ?뚮씪誘명꽣? ?쒖? NotFound ?덉쇅 泥섎━ 諛⑹떇???뺤씤?⑸땲??")
    public ResponseEntity<XyzSampleResponse> getSample(@RequestParam Long sampleId) {
        return ResponseEntity.ok(xyzSampleService.getSample(sampleId));
    }

    /**
     * ?깅줉 媛쒕컻 ?⑦꽩?낅땲??
     *
     * @param request ?깅줉 ?붿껌
     * @return ?깅줉???섑뵆
     */
    @PostMapping("/samples")
    @FpsTransaction(id = "XYZ02EDU0001", name = "XYZ援먯쑁?섑뵆?깅줉")
    @Operation(summary = "XYZ 援먯쑁 ?섑뵆 ?깅줉", description = "Body DTO瑜??댁슜???깅줉 嫄곕옒? ?쒖? 嫄곕옒ID 遺??諛⑹떇???뺤씤?⑸땲??")
    public ResponseEntity<XyzSampleResponse> createSample(@RequestBody XyzSampleRequest request) {
        return ResponseEntity.ok(xyzSampleService.createSample(request));
    }

    /**
     * ?섏젙 媛쒕컻 ?⑦꽩?낅땲??
     *
     * @param sampleId ?섏젙???섑뵆 ID
     * @param request  ?섏젙 ?붿껌
     * @return ?섏젙???섑뵆
     */
    @PutMapping("/samples")
    @FpsTransaction(id = "XYZ03EDU0001", name = "XYZ援먯쑁?섑뵆?섏젙")
    @Operation(summary = "XYZ 援먯쑁 ?섑뵆 ?섏젙", description = "?앸퀎?먮뒗 荑쇰━ ?뚮씪誘명꽣濡? 蹂寃?媛믪? Body濡?諛쏅뒗 ?섏젙 嫄곕옒 ?쒖????뺤씤?⑸땲??")
    public ResponseEntity<XyzSampleResponse> updateSample(
            @RequestParam Long sampleId,
            @RequestBody XyzSampleRequest request) {
        return ResponseEntity.ok(xyzSampleService.updateSample(sampleId, request));
    }

    /**
     * ??젣 媛쒕컻 ?⑦꽩?낅땲??
     *
     * @param sampleId ??젣???섑뵆 ID
     * @return ??젣 寃곌낵
     */
    @DeleteMapping("/samples")
    @FpsTransaction(id = "XYZ04EDU0001", name = "XYZ援먯쑁?섑뵆??젣")
    @Operation(summary = "XYZ 援먯쑁 ?섑뵆 ??젣", description = "紐낆떆 ?뚮씪誘명꽣 湲곕컲 ??젣 嫄곕옒? ??젣 ???쒖? ?덉쇅 ?먮쫫???뺤씤?⑸땲??")
    public ResponseEntity<Map<String, Object>> deleteSample(@RequestParam Long sampleId) {
        xyzSampleService.deleteSample(sampleId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deleted", true);
        response.put("sampleId", sampleId);
        return ResponseEntity.ok(response);
    }
}

