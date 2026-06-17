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
 * 신규 업무 모듈 개발자가 가장 먼저 참고하는 CRUD 교육 API입니다.
 *
 * <p>Controller, Service, DTO, 예외, 거래 로그 ID 부여 방식을 한 화면에서 확인할 수 있도록
 * 메모리 기반 샘플 저장소를 사용합니다. 실제 업무 모듈에서는 같은 패턴에 Mapper 또는 Repository를
 * 연결하면 됩니다.</p>
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 01. CRUD 샘플", description = "조회, 상세, 등록, 수정, 삭제 API 작성 기준")
public class XyzCrudEducationController {
    private final XyzSampleService xyzSampleService;

    public XyzCrudEducationController(XyzSampleService xyzSampleService) {
        this.xyzSampleService = xyzSampleService;
    }

    /**
     * 목록 조회 API의 기본 구조를 보여줍니다.
     *
     * @return 샘플 목록
     */
    @GetMapping("/samples")
    @FpsTransaction(id = "XYZ01EDU0001", name = "XYZ교육샘플목록조회")
    @Operation(summary = "샘플 목록 조회", description = "readOnly 조회 API와 표준 거래 로그 기록 방식을 확인합니다.")
    public ResponseEntity<List<XyzSampleResponse>> findSamples() {
        return ResponseEntity.ok(xyzSampleService.findSamples());
    }

    /**
     * 단건 상세 조회 API의 기본 구조를 보여줍니다.
     *
     * @param sampleId 샘플 ID
     * @return 샘플 상세
     */
    @GetMapping("/samples/detail")
    @FpsTransaction(id = "XYZ01EDU0002", name = "XYZ교육샘플상세조회")
    @Operation(summary = "샘플 상세 조회", description = "필수 파라미터 검증과 NotFound 예외 처리 흐름을 확인합니다.")
    public ResponseEntity<XyzSampleResponse> getSample(@RequestParam Long sampleId) {
        return ResponseEntity.ok(xyzSampleService.getSample(sampleId));
    }

    /**
     * 등록 API의 기본 구조를 보여줍니다.
     *
     * @param request 등록 요청
     * @return 등록된 샘플
     */
    @PostMapping("/samples")
    @FpsTransaction(id = "XYZ02EDU0001", name = "XYZ교육샘플등록")
    @Operation(summary = "샘플 등록", description = "Body DTO를 받아 신규 데이터를 생성하는 API 작성 방식을 확인합니다.")
    public ResponseEntity<XyzSampleResponse> createSample(@RequestBody XyzSampleRequest request) {
        return ResponseEntity.ok(xyzSampleService.createSample(request));
    }

    /**
     * 수정 API의 기본 구조를 보여줍니다.
     *
     * @param sampleId 수정 대상 샘플 ID
     * @param request 수정 요청
     * @return 수정된 샘플
     */
    @PutMapping("/samples")
    @FpsTransaction(id = "XYZ03EDU0001", name = "XYZ교육샘플수정")
    @Operation(summary = "샘플 수정", description = "식별자와 Body DTO를 함께 받아 데이터를 수정하는 API 작성 방식을 확인합니다.")
    public ResponseEntity<XyzSampleResponse> updateSample(
            @RequestParam Long sampleId,
            @RequestBody XyzSampleRequest request) {
        return ResponseEntity.ok(xyzSampleService.updateSample(sampleId, request));
    }

    /**
     * 삭제 API의 기본 구조를 보여줍니다.
     *
     * @param sampleId 삭제 대상 샘플 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/samples")
    @FpsTransaction(id = "XYZ04EDU0001", name = "XYZ교육샘플삭제")
    @Operation(summary = "샘플 삭제", description = "삭제 API와 삭제 결과 응답 작성 방식을 확인합니다.")
    public ResponseEntity<Map<String, Object>> deleteSample(@RequestParam Long sampleId) {
        xyzSampleService.deleteSample(sampleId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deleted", true);
        response.put("sampleId", sampleId);
        return ResponseEntity.ok(response);
    }
}
