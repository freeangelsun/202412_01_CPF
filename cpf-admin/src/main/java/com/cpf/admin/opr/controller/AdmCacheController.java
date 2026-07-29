package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.dto.*;
import com.cpf.admin.opr.service.*;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.logging.CpfTransactionContext;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** Provider 상태, Durable 무효화, 위험 캐시 조치를 제공하는 ADM API입니다. */
@RestController @RequestMapping("/adm/api/cache") @Tag(name="ADM-OPR Cache",description="Provider health/metrics, durable invalidation and controlled eviction")
public class AdmCacheController extends com.cpf.admin.common.base.AdmBaseController {
 private final AdmCacheOperationService service;
 private final AdmAuditLogService audit;
 public AdmCacheController(AdmCacheOperationService service,AdmAuditLogService audit){this.service=service;this.audit=audit;}
 @GetMapping("/summary") @CpfOnlineTransaction(id="OADMOP0010",name="ADMCacheSummary") @Operation(operationId="admCacheSummary",summary="Cache 운영 요약")
 public AdmCacheSummaryResponse summary(HttpServletRequest request){requireOperator(request);return service.summary();}
 @PostMapping("/refresh") @CpfOnlineTransaction(id="OADMOP0011",name="ADMCacheRefresh")
 public AdmCacheControlResponse refresh(@RequestParam(defaultValue="ALL")String target,@RequestParam String reason,HttpServletRequest request){return audited("CACHE_REFRESH",target,reason,request,(op,
         r)->service.refresh(target,op,r));}
 @PostMapping("/evict-key") @CpfOnlineTransaction(id="OADMOP0012",name="ADMCacheEvictKey")
 public AdmCacheControlResponse evictKey(@RequestBody EvictKeyRequest body,HttpServletRequest request){return audited("CACHE_EVICT_KEY",body.namespace()+":"+body.key(),body.reason(),request,(op,
         r)->service.evictKey(body.tenantId(),body.namespace(),body.key(),body.version(),op,r));}
 @PostMapping("/evict-namespace") @CpfOnlineTransaction(id="OADMOP0013",name="ADMCacheEvictNamespace")
 public AdmCacheControlResponse evictNamespace(@RequestBody EvictNamespaceRequest body,HttpServletRequest request){return audited("CACHE_EVICT_NAMESPACE",body.namespace(),body.reason(),request,(op,
         r)->service.evictNamespace(body.tenantId(),body.namespace(),body.version(),op,r));}
 @PostMapping("/reconcile") @CpfOnlineTransaction(id="OADMOP0014",name="ADMCacheReconcile")
 public AdmCacheControlResponse reconcile(@RequestBody ControlRequest body,HttpServletRequest request){return audited("CACHE_RECONCILE","DURABLE",body.reason(),request,service::reconcile);}
 private AdmCacheControlResponse audited(String action,String target,String reason,HttpServletRequest request,CacheAction operation){String r=audit.requireReason(reason),op=requireOperator(request);
         AdmCacheControlResponse result=operation.run(op,r);audit.record(CpfTransactionContext.transactionId(),op,action,"cache",target,r,request.getRemoteAddr());return result;}
 @FunctionalInterface
 private interface CacheAction{AdmCacheControlResponse run(String operator,String reason);}
 public record EvictKeyRequest(String tenantId,String namespace,String key,long version,String reason){}
    public record EvictNamespaceRequest(String tenantId,String namespace,long version,String
         reason){}
    public record ControlRequest(String reason){}
}
