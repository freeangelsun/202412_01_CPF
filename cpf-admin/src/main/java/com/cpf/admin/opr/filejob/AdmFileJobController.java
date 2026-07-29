package com.cpf.admin.opr.filejob;

import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.tabular.CpfTabularFormat;
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
@RestController
@RequestMapping("/adm/api/file-jobs")
@Tag(name="ADM-FileJob",description="Streaming CSV/XLSX Upload·Dry-run·Apply·Retry·Cancel·Rollback·Retention")
public class AdmFileJobController {
    private final AdmFileJobService service;
    public AdmFileJobController(AdmFileJobService service){this.service=service;}

    @GetMapping @CpfOnlineTransaction(id="OADMFL0010",name="AdmFileJobList")
    public List<AdmFileJobResponse> list(@RequestParam(defaultValue="100")int limit,HttpServletRequest request){
        operator(request);return service.list(limit);
    }
    @GetMapping("/{jobId}") @CpfOnlineTransaction(id="OADMFL0020",name="AdmFileJobDetail")
    public AdmFileJobResponse get(@PathVariable String jobId,HttpServletRequest request){operator(request);return service.get(jobId);}
    @GetMapping("/{jobId}/rows") @CpfOnlineTransaction(id="OADMFL0030",name="AdmFileJobRows")
    public List<AdmFileJobRowResponse> rows(@PathVariable String jobId,HttpServletRequest request){operator(request);return service.rows(jobId);}

    @PostMapping(path="/uploads",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @CpfOnlineTransaction(id="OADMFL0040",name="AdmFileUpload")
    @Operation(summary="Upload Job 접수",description="Template/version/header/security 검증을 비동기로 수행합니다.")
    public AdmFileJobResponse upload(@RequestParam String operationId,@RequestParam String templateCode,
            @RequestParam int templateVersion,@RequestParam CpfTabularFormat format,
            @RequestParam(defaultValue="true")boolean dryRun,@RequestParam String reason,
            @RequestPart MultipartFile file,HttpServletRequest request){
        if(file.isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"빈 파일은 허용하지 않습니다.");
        return service.upload(operationId,templateCode,templateVersion,format,dryRun,file,operator(request),reason,request.getRemoteAddr());
    }

    @PostMapping("/{jobId}/apply") @CpfOnlineTransaction(id="OADMFL0050",name="AdmFileJobApply")
    public AdmFileJobResponse apply(@PathVariable String jobId,@RequestBody ControlRequest body,HttpServletRequest request){
        return service.apply(jobId,operator(request),body.reason(),body.approvalId());
    }
    @PostMapping("/{jobId}/retry") @CpfOnlineTransaction(id="OADMFL0060",name="AdmFileJobRetry")
    public AdmFileJobResponse retry(@PathVariable String jobId,@RequestBody ControlRequest body,HttpServletRequest request){
        return service.retry(jobId,operator(request),body.reason());
    }
    @PostMapping("/{jobId}/cancel") @CpfOnlineTransaction(id="OADMFL0070",name="AdmFileJobCancel")
    public AdmFileJobResponse cancel(@PathVariable String jobId,@RequestBody ControlRequest body,HttpServletRequest request){
        return service.cancel(jobId,operator(request),body.reason());
    }
    @PostMapping("/{jobId}/rollback") @CpfOnlineTransaction(id="OADMFL0080",name="AdmFileJobRollback")
    public AdmFileJobResponse rollback(@PathVariable String jobId,@RequestBody ControlRequest body,HttpServletRequest request){
        return service.rollback(jobId,operator(request),body.reason(),body.approvalId());
    }
    @PostMapping("/{jobId}/resolve-unknown") @CpfOnlineTransaction(id="OADMFL0085",name="AdmFileJobResolveUnknown")
    @Operation(summary="결과 불명 행 확정",description="운영자가 외부/DB Side Effect를 확인한 뒤에만 결과 불명 행을 확정합니다.")
    public AdmFileJobResponse resolveUnknown(@PathVariable String jobId,@RequestBody UnknownResolutionRequest body,HttpServletRequest request){
        return service.resolveUnknown(jobId,body.rowNumber(),body.resolution(),body.businessKey(),body.rollbackToken(),
                operator(request),body.reason(),body.approvalId());
    }

    @GetMapping("/{jobId}/artifact") @CpfOnlineTransaction(id="OADMFL0090",name="AdmFileJobArtifact")
    public ResponseEntity<InputStreamResource> artifact(@PathVariable String jobId,HttpServletRequest request)throws Exception{
        operator(request);var path=service.artifact(jobId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(path.getFileName().toString(), java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .contentLength(Files.size(path)).contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(Files.newInputStream(path)));
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
