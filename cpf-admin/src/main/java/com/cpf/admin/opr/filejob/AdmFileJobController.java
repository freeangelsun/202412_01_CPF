package com.cpf.admin.opr.filejob;

import com.cpf.web.api.CpfController;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.file.tabular.api.CpfTabularFormat;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.Files;
import java.util.List;

/** ADM 대량 Upload/Download Job 운영 API입니다. */
@CpfController
@RequestMapping("/adm/api/file-jobs")
@Tag(name="ADM-FileJob",description="Streaming CSV/XLSX Upload·Dry-run·Apply·Retry·Cancel·Rollback·Retention")
public class AdmFileJobController {
    private final AdmFileJobService service;
    public AdmFileJobController(AdmFileJobService service){this.service=service;}

    @GetMapping
    @CpfOnlineTransaction(id = "OADMFL0010", name = "AdmFileJobList", ownerDomain="ADM")
    @Operation(operationId = "admFileJobList", summary = "File Job 목록 조회")
    public List<AdmFileJobResponse> list(@RequestParam(defaultValue="100")int limit,HttpServletRequest request){
        operator(request);return service.list(limit);
    }
    @GetMapping("/{jobId}")
    @CpfOnlineTransaction(id = "OADMFL0020", name = "AdmFileJobDetail", ownerDomain="ADM")
    @Operation(operationId = "admFileJobDetail", summary = "File Job 상세 조회")
    public AdmFileJobResponse get(@PathVariable String jobId,HttpServletRequest request){operator(request);return service.get(jobId);}
    @GetMapping("/{jobId}/rows")
    @CpfOnlineTransaction(id = "OADMFL0030", name = "AdmFileJobRows", ownerDomain="ADM")
    @Operation(operationId = "admFileJobRows", summary = "File Job 처리 행 조회")
    public List<AdmFileJobRowResponse> rows(@PathVariable String jobId,HttpServletRequest request){operator(request);return service.rows(jobId);}

    @PostMapping(path="/uploads",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @CpfOnlineTransaction(id="OADMFL0040",name="AdmFileUpload", ownerDomain="ADM")
    @Operation(
            operationId = "admFileJobUpload",
            summary = "Upload Job 접수",
            description = "Template/version/header/security 검증을 비동기로 수행합니다.")
    public AdmFileJobResponse upload(@RequestParam String operationId,@RequestParam String templateCode,
            @RequestParam int templateVersion,@RequestParam CpfTabularFormat format,
            @RequestParam(defaultValue="true")boolean dryRun,@RequestParam String reason,
            @RequestPart MultipartFile file,HttpServletRequest request){
        if(file.isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"빈 파일은 허용하지 않습니다.");
        return service.upload(operationId,templateCode,templateVersion,format,dryRun,file,operator(request),reason,request.getRemoteAddr());
    }

    /**
     * 위험조치는 Approval Engine의 불변 Snapshot/독립 승인/Owner Command로만 실행합니다.
     * 기존 직접 URL은 호환성상 매핑만 남기되 항상 fail-closed 합니다.
     */
    @Hidden
    @PostMapping("/{jobId}/apply")
    @CpfOnlineTransaction(id = "OADMFL0050", name = "AdmFileJobApplyDirectRejected", ownerDomain="ADM")
    public AdmFileJobResponse apply(@PathVariable String jobId,@RequestBody ControlRequest body,HttpServletRequest request){
        throw approvalRequired();
    }
    @Hidden
    @PostMapping("/{jobId}/retry")
    @CpfOnlineTransaction(id = "OADMFL0060", name = "AdmFileJobRetryDirectRejected", ownerDomain="ADM")
    public AdmFileJobResponse retry(@PathVariable String jobId,@RequestBody ControlRequest body,HttpServletRequest request){
        throw approvalRequired();
    }
    @Hidden
    @PostMapping("/{jobId}/cancel")
    @CpfOnlineTransaction(id = "OADMFL0070", name = "AdmFileJobCancelDirectRejected", ownerDomain="ADM")
    public AdmFileJobResponse cancel(@PathVariable String jobId,@RequestBody ControlRequest body,HttpServletRequest request){
        throw approvalRequired();
    }
    @Hidden
    @PostMapping("/{jobId}/rollback")
    @CpfOnlineTransaction(id = "OADMFL0080", name = "AdmFileJobRollbackDirectRejected", ownerDomain="ADM")
    public AdmFileJobResponse rollback(@PathVariable String jobId,@RequestBody ControlRequest body,HttpServletRequest request){
        throw approvalRequired();
    }
    @Hidden
    @PostMapping("/{jobId}/resolve-unknown")
    @CpfOnlineTransaction(id = "OADMFL0085", name = "AdmFileJobResolveUnknownDirectRejected", ownerDomain="ADM")
    public AdmFileJobResponse resolveUnknown(@PathVariable String jobId,@RequestBody UnknownResolutionRequest body,HttpServletRequest request){
        throw approvalRequired();
    }

    @GetMapping("/{jobId}/artifact")
    @CpfOnlineTransaction(id = "OADMFL0090", name = "AdmFileJobArtifact", ownerDomain="ADM")
    @Operation(operationId = "admFileJobArtifact", summary = "File Job 결과 Artifact 다운로드")
    public ResponseEntity<InputStreamResource> artifact(@PathVariable String jobId,HttpServletRequest request)throws Exception{
        operator(request);var path=service.artifact(jobId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(path.getFileName().toString(), java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .contentLength(Files.size(path)).contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(Files.newInputStream(path)));
    }


    private ResponseStatusException approvalRequired(){
        return new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,
                "File Job 위험조치는 ADM Approval 요청·독립 승인·Owner Command 실행을 통해서만 수행할 수 있습니다.");
    }
    private String operator(HttpServletRequest request){
        Object value=request.getAttribute("adm.operatorId");
        if(value instanceof String s&&!s.isBlank())return s;
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"검증된 ADM operator session이 필요합니다.");
    }
    public record ControlRequest(String reason,String approvalId){}
    public record UnknownResolutionRequest(long rowNumber,AdmFileJobService.UnknownResolution resolution,
                                           String businessKey,String rollbackToken,String reason,String approvalId){}
}
