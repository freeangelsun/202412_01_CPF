package com.cpf.member.sampleitem.dto;

import com.cpf.member.common.contract.MemberRequest;
import com.cpf.core.api.security.CpfMasking;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Generated Domain Minimal CRUD의 업무 입력 계약입니다.
 * transactionId/idempotencyKey/actor/sequence는 Body에 중복하지 않고 cpf-core TransactionContext에서 공급합니다.
 */
public record MemberSampleCommand(
        @NotBlank @Size(max = 100) String sampleKey,
        @NotBlank @Size(max = 200) String itemName,
        @Pattern(regexp = "ACTIVE|INACTIVE") String statusCode,
        @PositiveOrZero long expectedVersion) implements MemberRequest {
    public MemberSampleCommand {
        sampleKey = requireText(sampleKey,"sampleKey"); itemName=requireText(itemName,"itemName");
        statusCode=defaultText(statusCode,"ACTIVE").toUpperCase(Locale.ROOT);
        if(!statusCode.equals("ACTIVE")&&!statusCode.equals("INACTIVE")) throw new IllegalArgumentException("statusCode는 ACTIVE 또는 INACTIVE여야 합니다.");
        if(expectedVersion<0) throw new IllegalArgumentException("expectedVersion은 0 이상이어야 합니다.");
    }
    public String maskedAuditKey(){ return CpfMasking.mask(sampleKey); }
    private static String requireText(String value,String field){ if(value==null||value.isBlank()) throw new IllegalArgumentException(field+"는 필수입니다."); return value.trim(); }
    private static String defaultText(String value,String fallback){ return value==null||value.isBlank()?fallback:value.trim(); }
}