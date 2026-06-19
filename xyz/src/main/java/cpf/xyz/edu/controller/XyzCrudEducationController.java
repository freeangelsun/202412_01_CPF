package cpf.xyz.edu.controller;

import cpf.pfw.common.logging.CpfTransaction;
import cpf.xyz.edu.dto.XyzCrudEducationRequest;
import cpf.xyz.edu.dto.XyzCrudEducationResponse;
import cpf.xyz.edu.service.XyzCrudEducationService;
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
 * 메모리 기반 교육 저장소를 사용합니다. 실제 업무 모듈에서는 같은 패턴에 Mapper 또는 Repository를
 * 연결하면 됩니다.</p>
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 01. CRUD 교육", description = "조회, 상세, 등록, 수정, 삭제 API 작성 기준")
public class XyzCrudEducationController {
    private final XyzCrudEducationService crudEducationService;

    public XyzCrudEducationController(XyzCrudEducationService crudEducationService) {
        this.crudEducationService = crudEducationService;
    }

    /**
     * 목록 조회 API의 기본 구조를 보여줍니다.
     *
     * @return 교육 항목 목록
     */
    @GetMapping("/crud-items")
    @CpfTransaction(id = "XYZ01EDU0001", name = "XYZ교육CRUD목록조회")
    @Operation(summary = "CRUD 교육 항목 목록 조회", description = "readOnly 조회 API와 표준 거래 로그 기록 방식을 확인합니다.")
    public ResponseEntity<List<XyzCrudEducationResponse>> findEducationItems() {
        return ResponseEntity.ok(crudEducationService.findEducationItems());
    }

    /**
     * 단건 상세 조회 API의 기본 구조를 보여줍니다.
     *
     * @param educationItemId 교육 항목 ID
     * @return 교육 항목 상세
     */
    @GetMapping("/crud-items/detail")
    @CpfTransaction(id = "XYZ01EDU0002", name = "XYZ교육CRUD상세조회")
    @Operation(summary = "CRUD 교육 항목 상세 조회", description = "필수 파라미터 검증과 NotFound 예외 처리 흐름을 확인합니다.")
    public ResponseEntity<XyzCrudEducationResponse> getEducationItem(@RequestParam Long educationItemId) {
        return ResponseEntity.ok(crudEducationService.getEducationItem(educationItemId));
    }

    /**
     * 등록 API의 기본 구조를 보여줍니다.
     *
     * @param request 등록 요청
     * @return 등록된 교육 항목
     */
    @PostMapping("/crud-items")
    @CpfTransaction(id = "XYZ02EDU0001", name = "XYZ교육CRUD등록")
    @Operation(summary = "CRUD 교육 항목 등록", description = "Body DTO를 받아 신규 데이터를 생성하는 API 작성 방식을 확인합니다.")
    public ResponseEntity<XyzCrudEducationResponse> createEducationItem(@RequestBody XyzCrudEducationRequest request) {
        return ResponseEntity.ok(crudEducationService.createEducationItem(request));
    }

    /**
     * 수정 API의 기본 구조를 보여줍니다.
     *
     * @param educationItemId 수정 대상 교육 항목 ID
     * @param request 수정 요청
     * @return 수정된 교육 항목
     */
    @PutMapping("/crud-items")
    @CpfTransaction(id = "XYZ03EDU0001", name = "XYZ교육CRUD수정")
    @Operation(summary = "CRUD 교육 항목 수정", description = "식별자와 Body DTO를 함께 받아 데이터를 수정하는 API 작성 방식을 확인합니다.")
    public ResponseEntity<XyzCrudEducationResponse> updateEducationItem(
            @RequestParam Long educationItemId,
            @RequestBody XyzCrudEducationRequest request) {
        return ResponseEntity.ok(crudEducationService.updateEducationItem(educationItemId, request));
    }

    /**
     * 삭제 API의 기본 구조를 보여줍니다.
     *
     * @param educationItemId 삭제 대상 교육 항목 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/crud-items")
    @CpfTransaction(id = "XYZ04EDU0001", name = "XYZ교육CRUD삭제")
    @Operation(summary = "CRUD 교육 항목 삭제", description = "삭제 API와 삭제 결과 응답 작성 방식을 확인합니다.")
    public ResponseEntity<Map<String, Object>> deleteEducationItem(@RequestParam Long educationItemId) {
        crudEducationService.deleteEducationItem(educationItemId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deleted", true);
        response.put("educationItemId", educationItemId);
        return ResponseEntity.ok(response);
    }
}
