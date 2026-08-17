package com.cpf.admin.opr.controller;

import org.springframework.web.bind.annotation.RestController;
import com.cpf.admin.opr.dto.*;
import com.cpf.admin.opr.service.*;
import com.cpf.core.api.context.CpfContexts;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

/** Provider 상태, Durable 무효화, 위험 캐시 조치를 제공하는 ADM API입니다. */
@RestController @Validated @RequestMapping("/adm/api/cache") @Tag(name="ADM-OPR Cache",description="Provider health/metrics, durable invalidation and controlled eviction")
public class AdmCacheController extends com.cpf.admin.common.base.AdmBaseController {
 private final AdmCacheOperationService service;
 public AdmCacheController(AdmCacheOperationService service){this.service=service;}
 @GetMapping("/summary")@Operation(operationId="admCacheSummary",summary="Cache 운영 요약")
 public AdmCacheSummaryResponse summary(HttpServletRequest request){requireOperator(request);return service.summary();}
 @Hidden @PostMapping("/refresh")@Operation(operationId="admCacheRefresh", summary="Cache Provider 상태와 durable invalidation을 새로고침")
 public AdmCacheControlResponse refresh(@RequestParam(defaultValue="ALL")String target,@RequestParam String reason,HttpServletRequest request){requireOperator(request);throw approvalRequired();}
 @Hidden @PostMapping("/evict-key")@Operation(operationId="admCacheEvictKey", summary="Version CAS 기반 Cache Key 제거")
 public AdmCacheControlResponse evictKey(@Valid @RequestBody EvictKeyRequest body,HttpServletRequest request){requireOperator(request);throw approvalRequired();}
 @Hidden @PostMapping("/evict-namespace")@Operation(operationId="admCacheEvictNamespace", summary="Version CAS 기반 Cache Namespace 제거")
 public AdmCacheControlResponse evictNamespace(@Valid @RequestBody EvictNamespaceRequest body,HttpServletRequest request){requireOperator(request);throw approvalRequired();}
 @Hidden @PostMapping("/reconcile")@Operation(operationId="admCacheReconcile", summary="Durable invalidation과 Provider 상태 대사")
 public AdmCacheControlResponse reconcile(@Valid @RequestBody ControlRequest body,HttpServletRequest request){requireOperator(request);throw approvalRequired();}
 private ResponseStatusException approvalRequired(){
         return new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,
                 "Cache Refresh/Evict/Reconcile은 Approval Engine의 CACHE_* Owner Command로 실행해야 합니다.");
 }
  public record EvictKeyRequest(@NotBlank String tenantId,@NotBlank String namespace,@NotBlank String key,
         @PositiveOrZero long version,@NotBlank String reason){}
    public record EvictNamespaceRequest(@NotBlank String tenantId,@NotBlank String namespace,
         @PositiveOrZero long version,@NotBlank String reason){}
    public record ControlRequest(@NotBlank String reason){}
}
