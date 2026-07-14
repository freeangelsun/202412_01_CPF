package cpf.xyz.edu.controller;

import cpf.pfw.common.logging.CpfTransaction;
import cpf.xyz.edu.dto.XyzCrudEducationRequest;
import cpf.xyz.edu.dto.XyzCrudEducationResponse;
import cpf.xyz.edu.dto.XyzCrudEducationStatusRequest;
import cpf.xyz.edu.service.XyzCrudEducationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
 * 신규 업무 모듈 개발자가 참고하는 CRUD 교육 API입니다.
 *
 * <p>Controller, Service, Repository, Mapper, SQL fixture, 단위 테스트가 실제로 이어지는 샘플입니다.
 * `/xyz/edu/items`는 신규 표준 경로이고, 기존 문서/샘플 호환을 위해 `/xyz/edu/crud-items`도 함께 제공합니다.</p>
 */
@Validated
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 01. CRUD 교육", description = "목록, 상세, 등록, 수정, 상태 변경, 논리 삭제 API 작성 기준")
public class XyzCrudEducationController {
    private final XyzCrudEducationService crudEducationService;

    public XyzCrudEducationController(XyzCrudEducationService crudEducationService) {
        this.crudEducationService = crudEducationService;
    }

    /**
     * 검색, 상태, 정렬, limit을 받는 목록 조회 샘플입니다.
     */
    @GetMapping({"/items", "/crud-items"})
    @CpfTransaction(id = "XYZ01EDU0001", name = "XYZ교육CRUD목록조회")
    @Operation(operationId = "xyzCrudEducationFindEducationItems", summary = "CRUD 교육 항목 목록 조회", description = "DB Mapper 기반 목록 조회, 검색, 상태 필터, 정렬 whitelist, limit 제한을 확인합니다.")
    public ResponseEntity<List<XyzCrudEducationResponse>> findEducationItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String statusCode,
            @RequestParam(defaultValue = "idAsc") String sort,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(crudEducationService.findEducationItems(keyword, statusCode, sort, limit));
    }

    /**
     * PathVariable 기반 상세 조회 샘플입니다.
     */
    @GetMapping("/items/{educationItemId}")
    @CpfTransaction(id = "XYZ01EDU0002", name = "XYZ교육CRUD상세조회")
    @Operation(operationId = "xyzCrudEducationGetEducationItemByPath", summary = "CRUD 교육 항목 상세 조회", description = "PathVariable 검증과 NotFound 예외 처리 흐름을 확인합니다.")
    public ResponseEntity<XyzCrudEducationResponse> getEducationItemByPath(
            @PathVariable @Min(1) Long educationItemId) {
        return ResponseEntity.ok(crudEducationService.getEducationItem(educationItemId));
    }

    /**
     * 기존 `/crud-items/detail` 경로와의 호환을 위한 상세 조회 샘플입니다.
     */
    @GetMapping("/crud-items/detail")
    @CpfTransaction(id = "XYZ01EDU0003", name = "XYZ교육CRUD상세조회호환")
    @Operation(operationId = "xyzCrudEducationGetEducationItem", summary = "CRUD 교육 항목 상세 조회 호환", description = "기존 RequestParam 기반 상세 조회 경로를 유지합니다.")
    public ResponseEntity<XyzCrudEducationResponse> getEducationItem(
            @RequestParam @Min(1) Long educationItemId) {
        return ResponseEntity.ok(crudEducationService.getEducationItem(educationItemId));
    }

    /**
     * Bean Validation이 적용된 등록 샘플입니다.
     */
    @PostMapping({"/items", "/crud-items"})
    @CpfTransaction(id = "XYZ02EDU0001", name = "XYZ교육CRUD등록")
    @Operation(operationId = "xyzCrudEducationCreateEducationItem", summary = "CRUD 교육 항목 등록", description = "요청 DTO 검증 후 DB Mapper insert를 수행합니다.")
    public ResponseEntity<XyzCrudEducationResponse> createEducationItem(
            @Valid @RequestBody XyzCrudEducationRequest request) {
        return ResponseEntity.ok(crudEducationService.createEducationItem(request));
    }

    /**
     * RESTful PathVariable 기반 수정 샘플입니다.
     */
    @PutMapping("/items/{educationItemId}")
    @CpfTransaction(id = "XYZ03EDU0001", name = "XYZ교육CRUD수정")
    @Operation(operationId = "xyzCrudEducationUpdateEducationItemByPath", summary = "CRUD 교육 항목 수정", description = "PathVariable과 Body DTO를 함께 받아 DB Mapper update를 수행합니다.")
    public ResponseEntity<XyzCrudEducationResponse> updateEducationItemByPath(
            @PathVariable @Min(1) Long educationItemId,
            @Valid @RequestBody XyzCrudEducationRequest request) {
        return ResponseEntity.ok(crudEducationService.updateEducationItem(educationItemId, request));
    }

    /**
     * 기존 `/crud-items` 수정 경로와의 호환 샘플입니다.
     */
    @PutMapping("/crud-items")
    @CpfTransaction(id = "XYZ03EDU0002", name = "XYZ교육CRUD수정호환")
    @Operation(operationId = "xyzCrudEducationUpdateEducationItem", summary = "CRUD 교육 항목 수정 호환", description = "기존 RequestParam 기반 수정 경로를 유지합니다.")
    public ResponseEntity<XyzCrudEducationResponse> updateEducationItem(
            @RequestParam @Min(1) Long educationItemId,
            @Valid @RequestBody XyzCrudEducationRequest request) {
        return ResponseEntity.ok(crudEducationService.updateEducationItem(educationItemId, request));
    }

    /**
     * 상태 변경 전용 PATCH 샘플입니다.
     */
    @PatchMapping("/items/{educationItemId}/status")
    @CpfTransaction(id = "XYZ03EDU0003", name = "XYZ교육CRUD상태변경")
    @Operation(operationId = "xyzCrudEducationChangeEducationItemStatus", summary = "CRUD 교육 항목 상태 변경", description = "상태 변경 전용 DTO 검증 후 DB Mapper update를 수행합니다.")
    public ResponseEntity<XyzCrudEducationResponse> changeEducationItemStatus(
            @PathVariable @Min(1) Long educationItemId,
            @Valid @RequestBody XyzCrudEducationStatusRequest request) {
        return ResponseEntity.ok(crudEducationService.changeEducationItemStatus(educationItemId, request));
    }

    /**
     * 논리 삭제 샘플입니다.
     */
    @DeleteMapping({"/items/{educationItemId}", "/crud-items/{educationItemId}"})
    @CpfTransaction(id = "XYZ04EDU0001", name = "XYZ교육CRUD삭제")
    @Operation(operationId = "xyzCrudEducationDeleteEducationItemByPath", summary = "CRUD 교육 항목 논리 삭제", description = "실제 삭제 대신 use_yn=N, status=CLOSED로 전환하는 논리 삭제 기준을 확인합니다.")
    public ResponseEntity<Map<String, Object>> deleteEducationItemByPath(
            @PathVariable @Min(1) Long educationItemId,
            @RequestParam(required = false) String requestUser) {
        crudEducationService.deleteEducationItem(educationItemId, requestUser);
        return ResponseEntity.ok(deleteResponse(educationItemId));
    }

    /**
     * 기존 `/crud-items?educationItemId=` 삭제 경로와의 호환 샘플입니다.
     */
    @DeleteMapping("/crud-items")
    @CpfTransaction(id = "XYZ04EDU0002", name = "XYZ교육CRUD삭제호환")
    @Operation(operationId = "xyzCrudEducationDeleteEducationItem", summary = "CRUD 교육 항목 논리 삭제 호환", description = "기존 RequestParam 기반 삭제 경로를 유지합니다.")
    public ResponseEntity<Map<String, Object>> deleteEducationItem(
            @RequestParam @Min(1) Long educationItemId,
            @RequestParam(required = false) String requestUser) {
        crudEducationService.deleteEducationItem(educationItemId, requestUser);
        return ResponseEntity.ok(deleteResponse(educationItemId));
    }

    private Map<String, Object> deleteResponse(Long educationItemId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deleted", true);
        response.put("logicalDelete", true);
        response.put("educationItemId", educationItemId);
        return response;
    }
}
