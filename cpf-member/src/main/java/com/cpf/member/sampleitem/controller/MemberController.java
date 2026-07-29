package com.cpf.member.sampleitem.controller;

import com.cpf.member.common.base.MemberBaseController;
import com.cpf.member.sampleitem.dto.*;
import com.cpf.member.sampleitem.facade.MemberFacade;
import com.cpf.member.sampleitem.validation.MemberSearchValidator;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.page.CpfSlice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/** Generated Domain의 Typed Sample Item API입니다. */
@RestController
@RequestMapping("/api/v1/member/sample-items")
@Tag(name = "MBR Sample Item", description = "Generated Domain Typed CRUD/Search/Paging API")
public class MemberController extends MemberBaseController {
    private final MemberFacade facade;
    private final MemberSearchValidator validator;

    public MemberController(MemberFacade facade, MemberSearchValidator validator) {
        this.facade = Objects.requireNonNull(facade, "facade는 필수입니다.");
        this.validator = Objects.requireNonNull(validator, "validator는 필수입니다.");
    }

    @GetMapping
    @CpfOnlineTransaction(id = "OMBRQY0001", name = "MemberSearch", ownerDomain = "MBR")
    @Operation(operationId = "searchMemberSampleItems", summary = "Sample Item 목록 조회")
    public ResponseEntity<MemberSearchResult> search(MemberSearchRequest request) {
        validator.validate(request);
        return ok(facade.search(request));
    }

    @PostMapping
    @CpfOnlineTransaction(id = "OMBRIN0001", name = "MemberCreate", ownerDomain = "MBR")
    @Operation(operationId = "createMemberSampleItem", summary = "Sample Item 등록")
    public ResponseEntity<MemberSampleItem> create(@RequestBody MemberSampleCommand command) {
        return ok(facade.create(command));
    }

    @GetMapping("/{sampleKey}")
    @CpfOnlineTransaction(id = "OMBRQY0002", name = "MemberFind", ownerDomain = "MBR")
    @Operation(operationId = "findMemberSampleItem", summary = "Sample Item 단건 조회")
    public ResponseEntity<MemberSampleItem> findBySampleKey(@PathVariable String sampleKey) {
        return ok(facade.findBySampleKey(sampleKey)
                .orElseThrow(() -> new CpfValidationException("Sample Item을 찾을 수 없습니다.")));
    }

    @PostMapping("/{sampleItemId}/update")
    @CpfOnlineTransaction(id = "OMBRUP0001", name = "MemberUpdate", ownerDomain = "MBR")
    @Operation(operationId = "updateMemberSampleItem", summary = "낙관적 잠금 Sample Item 수정")
    public ResponseEntity<MemberSampleItem> update(
            @PathVariable long sampleItemId, @RequestBody MemberSampleCommand command) {
        return ok(facade.update(sampleItemId, command));
    }

    @PostMapping("/{sampleItemId}/delete")
    @CpfOnlineTransaction(id = "OMBRDL0001", name = "MemberDelete", ownerDomain = "MBR")
    @Operation(operationId = "deleteMemberSampleItem", summary = "낙관적 잠금 Sample Item 논리 삭제")
    public ResponseEntity<MemberDeleteResult> delete(
            @PathVariable long sampleItemId, @RequestBody MemberDeleteCommand command) {
        return ok(facade.delete(sampleItemId, command));
    }

    @GetMapping("/cursor")
    @CpfOnlineTransaction(id = "OMBRQY0003", name = "MemberCursor", ownerDomain = "MBR")
    public ResponseEntity<CpfSlice<MemberSampleItem>> cursor(
            @RequestParam(required = false) Long afterId, @RequestParam(defaultValue = "20") int size) {
        return ok(facade.cursor(afterId, size));
    }

    @PostMapping("/rollback-verify")
    @CpfOnlineTransaction(id = "OMBRTX0001", name = "MemberRollback", ownerDomain = "MBR")
    public ResponseEntity<Boolean> verifyRollback(@RequestBody MemberSampleCommand command) {
        return ok(facade.verifyRollback(command));
    }
}