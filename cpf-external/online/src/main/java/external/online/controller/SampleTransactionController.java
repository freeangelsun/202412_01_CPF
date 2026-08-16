package external.online.controller;

import external.online.base.ExternalBaseController;
import external.online.service.SampleTransactionService;
import external.domain.model.*;
import com.cpf.web.api.CpfController;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

/** CRUD/Search(Page·Slice·Cursor)를 제공하는 실제 Generated Business Controller입니다. */
@CpfController
@RequestMapping("/api/v1/external/samples")
public class SampleTransactionController extends ExternalBaseController {
    private final SampleTransactionService service;
    public SampleTransactionController(SampleTransactionService service) { this.service=service; }

    @PostMapping
    @Operation(operationId="createExternalSample", summary="external sample 생성")
    /** create 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SampleItem> create(@Valid @RequestBody CreateSampleRequest request) {
        SampleItem item=service.create(request); return created(URI.create("/api/v1/external/samples/"+item.getSampleItemId()),item);
    }
    @GetMapping("/{id}")
    @Operation(operationId="getExternalSample", summary="external sample 상세")
    public ResponseEntity<SampleItem> detail(@PathVariable long id) { return ok(service.detail(id)); }
    @GetMapping
    @Operation(operationId="searchExternalSamples", summary="external sample 검색")
    /** search 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SamplePage> search(@RequestParam(required=false) String keyword,
        @RequestParam(required=false) String statusCode, @RequestParam(defaultValue="0") Integer page,
        @RequestParam(defaultValue="20") Integer size) {
        return ok(service.search(new SampleSearchRequest(keyword,statusCode,page,normalizePageSize(size),null)));
    }
    @GetMapping("/slice")
    @Operation(operationId="sliceExternalSamples", summary="external sample cursor slice")
    /** slice 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SampleSlice> slice(@RequestParam(required=false) String keyword,
        @RequestParam(required=false) String statusCode, @RequestParam(defaultValue="0") Long cursor,
        @RequestParam(defaultValue="20") Integer size) {
        return ok(service.slice(new SampleSearchRequest(keyword,statusCode,null,normalizePageSize(size),cursor)));
    }
    @PutMapping("/{id}")
    @Operation(operationId="updateExternalSample", summary="external sample 수정")
    /** update 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<SampleItem> update(@PathVariable long id, @Valid @RequestBody UpdateSampleRequest request) { return ok(service.update(id,request)); }
    @DeleteMapping("/{id}")
    @Operation(operationId="deleteExternalSample", summary="external sample 논리 삭제")
    public ResponseEntity<SampleItem> delete(@PathVariable long id, @Valid @RequestBody DeleteSampleRequest request) {
        return ok(service.delete(id,request));
    }
}
