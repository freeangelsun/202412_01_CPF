package member.online.controller;

import member.online.base.MemberBaseController;
import member.online.service.SampleTransactionService;
import member.domain.model.*;
import com.cpf.web.api.CpfRestController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

/** CRUD/Search(Page·Slice·Cursor)를 제공하는 실제 Generated Business Controller입니다. */
@CpfRestController
@RequestMapping("/api/v1/member/samples")
public class SampleTransactionController extends MemberBaseController {
    private final SampleTransactionService service;
    public SampleTransactionController(SampleTransactionService service) { this.service=service; }

    @PostMapping
    @Operation(operationId="MBR_SAMPLE_TX_CREATE", summary="member sample 생성")
    @CpfOnlineTransaction(operationId="MBR_SAMPLE_TX_CREATE", name="member sample 생성", description="member Sample을 생성한다.")
    /** create 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SampleItem> create(@Valid @RequestBody CreateSampleRequest request) {
        SampleItem item=service.create(request); return created(URI.create("/api/v1/member/samples/"+item.getSampleItemId()),item);
    }
    @GetMapping("/{id}")
    @Operation(operationId="MBR_SAMPLE_TX_DETAIL", summary="member sample 상세")
    @CpfOnlineTransaction(operationId="MBR_SAMPLE_TX_DETAIL", name="member sample 상세", description="member Sample 상세를 조회한다.")
    public ResponseEntity<SampleItem> detail(@PathVariable long id) { return ok(service.detail(id)); }
    @GetMapping
    @Operation(operationId="MBR_SAMPLE_TX_SEARCH", summary="member sample 검색")
    @CpfOnlineTransaction(operationId="MBR_SAMPLE_TX_SEARCH", name="member sample 검색", description="member Sample을 조건·Paging으로 조회한다.")
    /** search 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SamplePage> search(@RequestParam(required=false) String keyword,
        @RequestParam(required=false) String statusCode, @RequestParam(defaultValue="0") Integer page,
        @RequestParam(defaultValue="20") Integer size) {
        return ok(service.search(new SampleSearchRequest(keyword,statusCode,page,normalizePageSize(size),null)));
    }
    @GetMapping("/slice")
    @Operation(operationId="MBR_SAMPLE_TX_SLICE", summary="member sample cursor slice")
    @CpfOnlineTransaction(operationId="MBR_SAMPLE_TX_SLICE", name="member sample cursor slice", description="member Sample을 Cursor Slice로 조회한다.")
    /** slice 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SampleSlice> slice(@RequestParam(required=false) String keyword,
        @RequestParam(required=false) String statusCode, @RequestParam(defaultValue="0") Long cursor,
        @RequestParam(defaultValue="20") Integer size) {
        return ok(service.slice(new SampleSearchRequest(keyword,statusCode,null,normalizePageSize(size),cursor)));
    }
    @PutMapping("/{id}")
    @Operation(operationId="MBR_SAMPLE_TX_UPDATE", summary="member sample 수정")
    @CpfOnlineTransaction(operationId="MBR_SAMPLE_TX_UPDATE", name="member sample 수정", description="member Sample을 낙관적 Version으로 수정한다.")
    /** update 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SampleItem> update(@PathVariable long id, @Valid @RequestBody UpdateSampleRequest request) { return ok(service.update(id,request)); }
    @DeleteMapping("/{id}")
    @Operation(operationId="MBR_SAMPLE_TX_DELETE", summary="member sample 논리 삭제")
    @CpfOnlineTransaction(operationId="MBR_SAMPLE_TX_DELETE", name="member sample 논리 삭제", description="member Sample을 논리 삭제한다.")
    public ResponseEntity<SampleItem> delete(@PathVariable long id, @Valid @RequestBody DeleteSampleRequest request) {
        return ok(service.delete(id,request));
    }
}
