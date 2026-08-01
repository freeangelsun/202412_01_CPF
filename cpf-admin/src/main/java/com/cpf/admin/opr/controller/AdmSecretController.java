package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.security.secret.CpfRotatableSecretProvider;
import com.cpf.core.api.security.secret.CpfSecretMetadata;
import com.cpf.core.api.security.secret.CpfSecretProvider;
import com.cpf.core.api.security.secret.CpfSecretReference;
import com.cpf.core.api.error.CpfNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Secret 운영 API. 원문 Secret 값은 어떤 API에서도 반환하지 않는다.
 * Provider별 metadata/rotation capability만 노출하고 rotation은 인증 Actor와 사유를 감사한다.
 */
@RestController
@RequestMapping("/adm/api/secrets")
public class AdmSecretController extends com.cpf.admin.common.base.AdmBaseController {
    private final List<CpfSecretProvider> providers;
    private final AdmAuditLogService audit;
    public AdmSecretController(List<CpfSecretProvider> providers, AdmAuditLogService audit) {
        this.providers=List.copyOf(providers); this.audit=audit;
    }

    @GetMapping("/providers")
    @CpfOnlineTransaction(id="OADMSE0001",name="ADMSecretProviders")
    @Operation(operationId="admSecretFindProviders", summary="Secret Provider 목록",description="Secret 원문이 아닌 Provider ID와 rotation 지원 여부만 반환합니다.")
    public ResponseEntity<List<Map<String,Object>>> providers(){
        return ResponseEntity.ok(providers.stream().map(p->Map.<String,Object>of("providerId",p.providerId(),"rotatable",p instanceof CpfRotatableSecretProvider)).toList());
    }

    @GetMapping("/metadata")
    @CpfOnlineTransaction(id="OADMSE0002",name="ADMSecretMetadata")
    @Operation(operationId="admSecretFindMetadata", summary="Secret Metadata 조회",description="원문 값을 반환하지 않고 존재/버전/만료/rotation metadata만 반환합니다.")
    public ResponseEntity<CpfSecretMetadata> metadata(@RequestParam String provider,@RequestParam String key,HttpServletRequest request){
        requireOperator(request);
        CpfSecretReference ref=new CpfSecretReference(provider,key);
        return ResponseEntity.ok(provider(provider).metadata(ref));
    }

    @PostMapping("/rotate")
    @CpfOnlineTransaction(id="OADMSE0003",name="ADMSecretRotate")
    @Operation(operationId="admSecretRotate", summary="Secret Rotation",description="Rotation 지원 Provider만 실행하며 Actor/Reason을 감사합니다. 원문은 반환하지 않습니다.")
    public ResponseEntity<CpfSecretMetadata> rotate(@RequestBody RotateRequest body,HttpServletRequest request){
        String actor=requireOperator(request); String reason=audit.requireReason(body.reason());
        CpfSecretProvider p=provider(body.provider());
        if(!(p instanceof CpfRotatableSecretProvider rotatable)) throw new IllegalArgumentException("Rotation을 지원하지 않는 Provider입니다.");
        CpfSecretReference ref=new CpfSecretReference(body.provider(),body.key());
        CpfSecretMetadata result=rotatable.rotate(ref,reason,actor);
        audit.record(CpfTransactionContext.transactionId(),actor,"SECRET_ROTATE","secret_reference",ref.toString(),reason,request.getRemoteAddr());
        return ResponseEntity.ok(result);
    }

    private CpfSecretProvider provider(String id){return providers.stream().filter(p->p.providerId().equalsIgnoreCase(id)).findFirst().orElseThrow(()->new CpfNotFoundException("Secret Provider를 찾을 수 없습니다: "+id));}
    public record RotateRequest(String provider,String key,String reason){}
}
