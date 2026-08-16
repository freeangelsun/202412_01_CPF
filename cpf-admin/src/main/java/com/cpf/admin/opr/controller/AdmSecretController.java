package com.cpf.admin.opr.controller;

import com.cpf.web.api.CpfController;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.security.api.secret.CpfRotatableSecretProvider;
import com.cpf.security.api.secret.CpfSecretMetadata;
import com.cpf.security.api.secret.CpfSecretProvider;
import com.cpf.security.api.secret.CpfSecretReference;
import com.cpf.core.api.error.CpfNotFoundException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Secret 운영 API. 원문 Secret 값은 어떤 API에서도 반환하지 않는다.
 * Provider별 metadata/rotation capability만 노출하고 rotation은 인증 Actor와 사유를 감사한다.
 */
@CpfController
@RequestMapping("/adm/api/secrets")
public class AdmSecretController extends com.cpf.admin.common.base.AdmBaseController {
    private final List<CpfSecretProvider> providers;
    public AdmSecretController(List<CpfSecretProvider> providers) {
        this.providers=List.copyOf(providers);
    }

    @GetMapping("/providers")
    @CpfOnlineTransaction(id="OADMSE0001",name="ADMSecretProviders", ownerDomain="ADM")
    @Operation(operationId="admSecretFindProviders", summary="Secret Provider 목록",description="Secret 원문이 아닌 Provider ID와 rotation 지원 여부만 반환합니다.")
    public ResponseEntity<List<Map<String,Object>>> providers(){
        return ResponseEntity.ok(providers.stream().map(p->Map.<String,Object>of("providerId",p.providerId(),"rotatable",p instanceof CpfRotatableSecretProvider)).toList());
    }

    @GetMapping("/metadata")
    @CpfOnlineTransaction(id="OADMSE0002",name="ADMSecretMetadata", ownerDomain="ADM")
    @Operation(operationId="admSecretFindMetadata", summary="Secret Metadata 조회",description="원문 값을 반환하지 않고 존재/버전/만료/rotation metadata만 반환합니다.")
    public ResponseEntity<CpfSecretMetadata> metadata(@RequestParam String provider,@RequestParam String key,HttpServletRequest request){
        requireOperator(request);
        CpfSecretReference ref=new CpfSecretReference(provider,key);
        return ResponseEntity.ok(provider(provider).metadata(ref));
    }

    /** Secret Rotation은 Approval Engine의 독립 승인/Owner Command로만 실행합니다. */
    @Hidden
    @PostMapping("/rotate")
    @CpfOnlineTransaction(id="OADMSE0003",name="ADMSecretRotateDirectRejected", ownerDomain="ADM")
    public ResponseEntity<CpfSecretMetadata> rotate(@RequestBody RotateRequest body,HttpServletRequest request){
        requireOperator(request);
        throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,
                "Secret Rotation은 ADM Approval 요청·독립 승인·Owner Command 실행을 통해서만 수행할 수 있습니다.");
    }

    private CpfSecretProvider provider(String id){return providers.stream().filter(p->p.providerId().equalsIgnoreCase(id)).findFirst().orElseThrow(()->new CpfNotFoundException("Secret Provider를 찾을 수 없습니다: "+id));}
    public record RotateRequest(String provider,String key,String reason){}
}
