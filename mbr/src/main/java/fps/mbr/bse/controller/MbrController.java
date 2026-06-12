package fps.mbr.bse.controller;

import fps.pfw.common.logging.FpsTransaction;
import fps.mbr.common.response.BaseResponse;
import fps.mbr.common.response.ResponseCode;
import fps.mbr.bse.dto.MbrDTO;
import fps.mbr.bse.service.MbrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

import java.util.List;

/**
 * 회원 REST API Controller
 * - 금융권 표준 설계: 모든 파라미터는 쿼리 파라미터로 명시적 전달
 * - 경로 변수 대신 쿼리 파라미터 사용으로 명확한 감시/로깅 지원
 * 
 * API 엔드포인트:
 * - GET  /mbr/list              : 회원 목록 조회
 * - GET  /mbr/detail?memberId   : 회원 상세 조회 (쿼리 파라미터)
 * - GET  /mbr/search?name       : 회원명 검색 (쿼리 파라미터)
 * - POST /mbr/create            : 회원 생성 (Body 파라미터)
 * - PUT  /mbr/update            : 회원 수정 (Body 파라미터)
 * - DELETE /mbr/delete?memberId : 회원 삭제 (쿼리 파라미터)
 * 
 * 설계 원칙:
 * 1. 모든 응답은 BaseResponse 통일된 형식 사용
 * 2. 입력값은 쿼리 파라미터로 명시적 전달 (로깅/감시 용이)
 * 3. 에러는 ApiException으로 처리 (GlobalExceptionHandler에서 통일)
 * 4. 요청자 ID는 나중에 SecurityContext에서 자동 추출 (현재 상수값)
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/mbr")
@RequiredArgsConstructor
@Validated
@Tag(name = "MBR-BSE 회원기본관리", description = "회원 목록/상세/검색/등록/수정/삭제 API")
public class MbrController {
    
    private final MbrService mbrService;
    
    /** 현재는 상수값, 나중에 SecurityContext에서 자동 주입 */
    private static final String REQUESTER_ID = "SYSTEM";
    
    /**
     * GET /mbr/list
     * 전체 회원 목록 조회
     * 
     * @return BaseResponse<List<MbrDTO>> 회원 목록
     */
    @GetMapping("/list")
    @FpsTransaction(id = "MBR01BSE0001", name = "회원기본목록조회")
    @Operation(summary = "회원 목록 조회", description = "MBR 기본회원 목록을 조회합니다.")
    public ResponseEntity<BaseResponse<List<MbrDTO>>> getList() {
        log.info("[MbrController] 회원 목록 조회 요청");
        
        List<MbrDTO> members = mbrService.getAllMembers();
        
        BaseResponse<List<MbrDTO>> response = BaseResponse.ok(ResponseCode.SUCCESS, members);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * GET /mbr/detail?memberId={memberId}
     * 회원 상세 조회 (쿼리 파라미터 사용)
     * 
     * 파라미터:
     *   - memberId (필수): 회원 ID (양수)
     * 
     * 예시:
     *   GET /mbr/detail?memberId=1
     * 
     * @param memberId 회원 ID
     * @return BaseResponse<MbrDTO> 회원 정보
     */
    @GetMapping("/detail")
    @FpsTransaction(id = "MBR01BSE0002", name = "회원기본상세조회")
    @Operation(summary = "회원 상세 조회", description = "경로변수 대신 memberId 쿼리 파라미터로 회원 상세를 조회합니다.")
    public ResponseEntity<BaseResponse<MbrDTO>> getDetail(
            @RequestParam(name = "memberId")
            @NotNull(message = "회원 ID는 필수입니다.")
            @Positive(message = "회원 ID는 양수여야 합니다.")
            Integer memberId) {
        
        log.info("[MbrController] 회원 상세 조회 요청 - memberId: {}", memberId);
        
        MbrDTO member = mbrService.getMemberById(memberId);
        
        BaseResponse<MbrDTO> response = BaseResponse.ok(ResponseCode.SUCCESS, member);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * GET /mbr/search?name={name}
     * 회원명으로 검색 조회 (쿼리 파라미터 사용)
     * 
     * 파라미터:
     *   - name (필수): 회원명 검색 키워드 (부분 검색)
     * 
     * 예시:
     *   GET /mbr/search?name=김
     * 
     * @param name 회원명 검색 키워드
     * @return BaseResponse<List<MbrDTO>> 검색 결과 목록
     */
    @GetMapping("/search")
    @FpsTransaction(id = "MBR01BSE0003", name = "회원기본명검색")
    @Operation(summary = "회원명 검색", description = "회원명을 명시 파라미터로 받아 부분 검색합니다.")
    public ResponseEntity<BaseResponse<List<MbrDTO>>> search(
            @RequestParam(name = "name")
            @NotBlank(message = "회원명은 필수입니다.")
            @Size(max = 100, message = "회원명은 100자 이하여야 합니다.")
            String name) {
        
        log.info("[MbrController] 회원명 검색 요청 - name: {}", name);
        
        List<MbrDTO> members = mbrService.searchMembersByName(name);
        
        BaseResponse<List<MbrDTO>> response = BaseResponse.ok(ResponseCode.SUCCESS, members);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * POST /mbr/create
     * 회원 생성 (Body 파라미터 사용)
     * 
     * 요청 Body:
     * {
     *   "memberName": "김철수",
     *   "description": "신규 회원"
     * }
     * 
     * @param memberCreateRequest 회원 생성 요청 DTO
     * @return BaseResponse<MbrDTO> 생성된 회원 정보
     */
    @PostMapping("/create")
    @FpsTransaction(id = "MBR02BSE0001", name = "회원기본등록")
    @Operation(summary = "회원 등록", description = "회원 생성 요청 Body로 신규 회원을 등록합니다.")
    public ResponseEntity<BaseResponse<MbrDTO>> create(
            @Valid @RequestBody MemberCreateRequest memberCreateRequest) {
        
        log.info("[MbrController] 회원 생성 요청 - memberName: {}", 
                memberCreateRequest.getMemberName());
        
        MbrDTO createdMember = mbrService.createMember(
                memberCreateRequest.getMemberName(),
                memberCreateRequest.getDescription(),
                REQUESTER_ID
        );
        
        BaseResponse<MbrDTO> response = BaseResponse.ok(ResponseCode.CREATED, createdMember);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * PUT /mbr/update
     * 회원 정보 수정 (Body 파라미터 사용)
     * 
     * 요청 Body:
     * {
     *   "memberId": 1,
     *   "memberName": "김철수 수정",
     *   "description": "수정된 설명"
     * }
     * 
     * @param memberUpdateRequest 회원 수정 요청 DTO
     * @return BaseResponse<MbrDTO> 수정된 회원 정보
     */
    @PutMapping("/update")
    @FpsTransaction(id = "MBR03BSE0001", name = "회원기본수정")
    @Operation(summary = "회원 수정", description = "회원 수정 요청 Body로 회원 기본 정보를 변경합니다.")
    public ResponseEntity<BaseResponse<MbrDTO>> update(
            @Valid @RequestBody MemberUpdateRequest memberUpdateRequest) {
        
        log.info("[MbrController] 회원 수정 요청 - memberId: {}", 
                memberUpdateRequest.getMemberId());
        
        MbrDTO updatedMember = mbrService.updateMember(
                memberUpdateRequest.getMemberId(),
                memberUpdateRequest.getMemberName(),
                memberUpdateRequest.getDescription(),
                REQUESTER_ID
        );
        
        BaseResponse<MbrDTO> response = BaseResponse.ok(ResponseCode.UPDATED, updatedMember);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * DELETE /mbr/delete?memberId={memberId}
     * 회원 삭제 (쿼리 파라미터 사용)
     * 
     * 파라미터:
     *   - memberId (필수): 회원 ID
     * 
     * 예시:
     *   DELETE /mbr/delete?memberId=1
     * 
     * @param memberId 회원 ID
     * @return BaseResponse 삭제 결과
     */
    @DeleteMapping("/delete")
    @FpsTransaction(id = "MBR04BSE0001", name = "회원기본삭제")
    @Operation(summary = "회원 삭제", description = "memberId 쿼리 파라미터로 회원 삭제 거래를 수행합니다.")
    public ResponseEntity<BaseResponse<Void>> delete(
            @RequestParam(name = "memberId")
            @NotNull(message = "회원 ID는 필수입니다.")
            @Positive(message = "회원 ID는 양수여야 합니다.")
            Integer memberId) {
        
        log.info("[MbrController] 회원 삭제 요청 - memberId: {}", memberId);
        
        mbrService.deleteMember(memberId, REQUESTER_ID);
        
        BaseResponse<Void> response = BaseResponse.ok(ResponseCode.DELETED);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    // ==================== 내부 Request DTO ====================
    
    /**
     * 회원 생성 요청 DTO
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MemberCreateRequest {
        @NotBlank(message = "회원명은 필수입니다.")
        @Size(max = 100, message = "회원명은 100자 이하여야 합니다.")
        private String memberName;

        @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
        private String description;
    }
    
    /**
     * 회원 수정 요청 DTO
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MemberUpdateRequest {
        @NotNull(message = "회원 ID는 필수입니다.")
        @Positive(message = "회원 ID는 양수여야 합니다.")
        private Integer memberId;

        @NotBlank(message = "회원명은 필수입니다.")
        @Size(max = 100, message = "회원명은 100자 이하여야 합니다.")
        private String memberName;

        @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
        private String description;
    }
}
