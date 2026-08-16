package member.online.controller;

import member.online.base.MemberBaseController;
import member.online.service.SampleTransactionService;
import member.domain.model.*;
import com.cpf.web.api.CpfController;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

/** CRUD/Search(Page·Slice·Cursor)를 제공하는 실제 Generated Business Controller입니다. */
@CpfController
@RequestMapping("/api/v1/member/samples")
public class SampleTransactionController extends MemberBaseController {
    private final SampleTransactionService service;
    public SampleTransactionController(SampleTransactionService service) { this.service=service; }

    @PostMapping
    @Operation(operationId="createMemberSample", summary="member sample 생성")
    /** create 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SampleItem> create(@Valid @RequestBody CreateSampleRequest request) {
        SampleItem item=service.create(request); return created(URI.create("/api/v1/member/samples/"+item.getSampleItemId()),item);
    }
    @GetMapping("/{id}")
    @Operation(operationId="getMemberSample", summary="member sample 상세")
    public ResponseEntity<SampleItem> detail(@PathVariable long id) { return ok(service.detail(id)); }
    @GetMapping
    @Operation(operationId="searchMemberSamples", summary="member sample 검색")
    /** search 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SamplePage> search(@RequestParam(required=false) String keyword,
        @RequestParam(required=false) String statusCode, @RequestParam(defaultValue="0") Integer page,
        @RequestParam(defaultValue="20") Integer size) {
        return ok(service.search(new SampleSearchRequest(keyword,statusCode,page,normalizePageSize(size),null)));
    }
    @GetMapping("/slice")
    @Operation(operationId="sliceMemberSamples", summary="member sample cursor slice")
    /** slice 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SampleSlice> slice(@RequestParam(required=false) String keyword,
        @RequestParam(required=false) String statusCode, @RequestParam(defaultValue="0") Long cursor,
        @RequestParam(defaultValue="20") Integer size) {
        return ok(service.slice(new SampleSearchRequest(keyword,statusCode,null,normalizePageSize(size),cursor)));
    }
    @PutMapping("/{id}")
    @Operation(operationId="updateMemberSample", summary="member sample 수정")
    /** update 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SampleItem> update(@PathVariable long id, @Valid @RequestBody UpdateSampleRequest request) { return ok(service.update(id,request)); }
    @DeleteMapping("/{id}")
    @Operation(operationId="deleteMemberSample", summary="member sample 논리 삭제")
    public ResponseEntity<SampleItem> delete(@PathVariable long id, @Valid @RequestBody DeleteSampleRequest request) {
        return ok(service.delete(id,request));
    }
}
