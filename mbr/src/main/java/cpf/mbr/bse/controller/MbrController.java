package cpf.mbr.bse.controller;

import cpf.mbr.bse.dto.MbrDTO;
import cpf.mbr.bse.service.MbrService;
import cpf.mbr.common.response.BaseResponse;
import cpf.mbr.common.response.ResponseCode;
import cpf.pfw.common.logging.FpsTransaction;
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
@Tag(name = "MBR-BSE Member", description = "Member list, detail, search, create, update, and delete sample APIs")
public class MbrController {

    private static final String REQUESTER_ID = "SYSTEM";

    private final MbrService mbrService;

    @GetMapping("/list")
    @FpsTransaction(id = "MBR01BSE0001", name = "MBRMemberList")
    @Operation(summary = "Member list", description = "Returns all sample members from MBR.")
    public ResponseEntity<BaseResponse<List<MbrDTO>>> getList() {
        log.info("[MbrController] member list requested");

        List<MbrDTO> members = mbrService.getAllMembers();
        BaseResponse<List<MbrDTO>> response = BaseResponse.ok(ResponseCode.SUCCESS, members);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/detail")
    @FpsTransaction(id = "MBR01BSE0002", name = "MBRMemberDetail")
    @Operation(summary = "Member detail", description = "Returns one member by memberId.")
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
    @FpsTransaction(id = "MBR01BSE0003", name = "MBRMemberSearch")
    @Operation(summary = "Member search", description = "Searches members by name.")
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
    @FpsTransaction(id = "MBR02BSE0001", name = "MBRMemberCreate")
    @Operation(summary = "Member create", description = "Creates a sample member.")
    public ResponseEntity<BaseResponse<MbrDTO>> create(@Valid @RequestBody MemberCreateRequest request) {
        log.info("[MbrController] member create requested. memberName={}", request.getMemberName());

        MbrDTO createdMember = mbrService.createMember(request.getMemberName(), request.getDescription(), REQUESTER_ID);
        BaseResponse<MbrDTO> response = BaseResponse.ok(ResponseCode.CREATED, createdMember);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @FpsTransaction(id = "MBR03BSE0001", name = "MBRMemberUpdate")
    @Operation(summary = "Member update", description = "Updates a sample member.")
    public ResponseEntity<BaseResponse<MbrDTO>> update(@Valid @RequestBody MemberUpdateRequest request) {
        log.info("[MbrController] member update requested. memberId={}", request.getMemberId());

        MbrDTO updatedMember = mbrService.updateMember(
                request.getMemberId(),
                request.getMemberName(),
                request.getDescription(),
                REQUESTER_ID);
        BaseResponse<MbrDTO> response = BaseResponse.ok(ResponseCode.UPDATED, updatedMember);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    @FpsTransaction(id = "MBR04BSE0001", name = "MBRMemberDelete")
    @Operation(summary = "Member delete", description = "Deletes a sample member by memberId.")
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

        @Size(max = 255, message = "description must be 255 characters or less")
        private String description;
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

        @Size(max = 255, message = "description must be 255 characters or less")
        private String description;
    }
}
