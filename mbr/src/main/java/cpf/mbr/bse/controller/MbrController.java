package cpf.mbr.bse.controller;

import cpf.mbr.bse.dto.MbrDTO;
import cpf.mbr.bse.service.MbrService;
import cpf.mbr.common.response.BaseResponse;
import cpf.mbr.common.response.ResponseCode;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

@Slf4j
@RestController
@RequestMapping("/mbr")
@RequiredArgsConstructor
@Validated
@Tag(name = "MBR-BSE Member", description = "회원 목록, 상세, 검색, 등록, 수정, 삭제 샘플 API")
public class MbrController {

    private static final String REQUESTER_ID = "SYSTEM";

    private final MbrService mbrService;

    @GetMapping("/list")
    @CpfOnlineTransaction(id = "OMBRMB0001", name = "MBRMemberList")
    @Operation(operationId = "mbrApiGetList", summary = "회원 목록 조회", description = "MBR 샘플 회원 목록을 조회합니다.")
    public ResponseEntity<BaseResponse<List<MbrDTO>>> getList() {
        log.info("[MbrController] member list requested");

        List<MbrDTO> members = mbrService.getAllMembers();
        BaseResponse<List<MbrDTO>> response = BaseResponse.ok(ResponseCode.SUCCESS, members);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/detail")
    @CpfOnlineTransaction(id = "OMBRMB0002", name = "MBRMemberDetail")
    @Operation(operationId = "mbrApiGetDetail", summary = "회원 상세 조회", description = "회원 내부 순번으로 회원 상세를 조회합니다.")
    public ResponseEntity<BaseResponse<MbrDTO>> getDetail(
            @RequestParam(name = "memberId")
            @NotNull(message = "memberId is required")
            @Positive(message = "memberId must be positive")
            Integer memberId) {

        log.info("[MbrController] member detail requested. memberId={}", memberId);

        MbrDTO member = mbrService.getMemberById(memberId);
        BaseResponse<MbrDTO> response = BaseResponse.ok(ResponseCode.SUCCESS, member);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/search")
    @CpfOnlineTransaction(id = "OMBRMB0003", name = "MBRMemberSearch")
    @Operation(operationId = "mbrApiSearch", summary = "회원 검색", description = "회원명으로 회원을 검색합니다.")
    public ResponseEntity<BaseResponse<List<MbrDTO>>> search(
            @RequestParam(name = "name")
            @NotBlank(message = "name is required")
            @Size(max = 100, message = "name must be 100 characters or less")
            String name) {

        log.info("[MbrController] member search requested. name={}", name);

        List<MbrDTO> members = mbrService.searchMembersByName(name);
        BaseResponse<List<MbrDTO>> response = BaseResponse.ok(ResponseCode.SUCCESS, members);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/create")
    @CpfOnlineTransaction(id = "OMBRMB0004", name = "MBRMemberCreate")
    @Operation(operationId = "mbrApiCreate", summary = "회원 등록", description = "MBR 샘플 회원을 등록합니다.")
    public ResponseEntity<BaseResponse<MbrDTO>> create(@Valid @RequestBody MemberCreateRequest request) {
        log.info("[MbrController] member create requested. memberName={}", request.getMemberName());

        MbrDTO createdMember = mbrService.createMember(request.toDto(), REQUESTER_ID);
        BaseResponse<MbrDTO> response = BaseResponse.ok(ResponseCode.CREATED, createdMember);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @CpfOnlineTransaction(id = "OMBRMB0005", name = "MBRMemberUpdate")
    @Operation(operationId = "mbrApiUpdate", summary = "회원 수정", description = "MBR 샘플 회원을 수정합니다.")
    public ResponseEntity<BaseResponse<MbrDTO>> update(@Valid @RequestBody MemberUpdateRequest request) {
        log.info("[MbrController] member update requested. memberId={}", request.getMemberId());

        MbrDTO updatedMember = mbrService.updateMember(request.toDto(), REQUESTER_ID);
        BaseResponse<MbrDTO> response = BaseResponse.ok(ResponseCode.UPDATED, updatedMember);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    @CpfOnlineTransaction(id = "OMBRMB0006", name = "MBRMemberDelete")
    @Operation(operationId = "mbrApiDelete", summary = "회원 삭제", description = "회원 내부 순번으로 샘플 회원을 삭제합니다.")
    public ResponseEntity<BaseResponse<Void>> delete(
            @RequestParam(name = "memberId")
            @NotNull(message = "memberId is required")
            @Positive(message = "memberId must be positive")
            Integer memberId) {

        log.info("[MbrController] member delete requested. memberId={}", memberId);

        mbrService.deleteMember(memberId, REQUESTER_ID);
        BaseResponse<Void> response = BaseResponse.ok(ResponseCode.DELETED);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberCreateRequest {
        @NotBlank(message = "memberName is required")
        @Size(max = 100, message = "memberName must be 100 characters or less")
        private String memberName;

        @Size(max = 30, message = "memberNo must be 30 characters or less")
        private String memberNo;

        @Size(max = 30, message = "customerNo must be 30 characters or less")
        private String customerNo;

        @Size(max = 100, message = "loginId must be 100 characters or less")
        private String loginId;

        @Size(max = 200, message = "email must be 200 characters or less")
        private String email;

        @Size(max = 30, message = "mobileNo must be 30 characters or less")
        private String mobileNo;

        private String channelCode;

        @Size(max = 255, message = "description must be 255 characters or less")
        private String description;

        private MbrDTO toDto() {
            return MbrDTO.builder()
                    .memberNo(memberNo)
                    .customerNo(customerNo)
                    .loginId(loginId)
                    .memberName(memberName)
                    .email(email)
                    .mobileNo(mobileNo)
                    .channelCode(channelCode)
                    .description(description)
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberUpdateRequest {
        @NotNull(message = "memberId is required")
        @Positive(message = "memberId must be positive")
        private Integer memberId;

        @NotBlank(message = "memberName is required")
        @Size(max = 100, message = "memberName must be 100 characters or less")
        private String memberName;

        @Size(max = 30, message = "memberNo must be 30 characters or less")
        private String memberNo;

        @Size(max = 30, message = "customerNo must be 30 characters or less")
        private String customerNo;

        @Size(max = 100, message = "loginId must be 100 characters or less")
        private String loginId;

        @Size(max = 200, message = "email must be 200 characters or less")
        private String email;

        @Size(max = 30, message = "mobileNo must be 30 characters or less")
        private String mobileNo;

        private String memberStatus;
        private String lockYn;
        private String withdrawYn;
        private String channelCode;

        @Size(max = 255, message = "description must be 255 characters or less")
        private String description;

        private MbrDTO toDto() {
            return MbrDTO.builder()
                    .memberId(memberId)
                    .memberNo(memberNo)
                    .customerNo(customerNo)
                    .loginId(loginId)
                    .memberName(memberName)
                    .email(email)
                    .mobileNo(mobileNo)
                    .memberStatus(memberStatus)
                    .lockYn(lockYn)
                    .withdrawYn(withdrawYn)
                    .channelCode(channelCode)
                    .description(description)
                    .build();
        }
    }
}
