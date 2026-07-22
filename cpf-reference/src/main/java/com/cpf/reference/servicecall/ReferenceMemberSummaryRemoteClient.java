package com.cpf.reference.servicecall;

import com.cpf.core.common.http.CpfWebClient;
import com.cpf.core.common.servicecall.CpfServiceCallOptions;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * MBR remote 호출의 HTTP 세부정보를 캡슐화하는 adapter입니다.
 */
@Component
public class ReferenceMemberSummaryRemoteClient implements ReferenceMemberSummaryClient {
    private final CpfWebClient webClient;

    /**
     * remote adapter를 생성합니다.
     *
     * @param webClient CPF 서비스 호출 경계
     */
    public ReferenceMemberSummaryRemoteClient(CpfWebClient webClient) {
        this.webClient = Objects.requireNonNull(webClient, "webClient는 필수입니다.");
    }

    /**
     * 중앙 정책을 유지하며 MBR 회원 요약을 조회합니다.
     *
     * @param request 업무 요청
     * @param options named policy 옵션
     * @return typed 회원 요약
     */
    @Override
    public ReferenceMemberSummaryResponse execute(
            ReferenceMemberSummaryRequest request,
            CpfServiceCallOptions options) {
        Objects.requireNonNull(request, "request는 필수입니다.");
        Objects.requireNonNull(options, "options는 필수입니다.");
        Map<?, ?> result = webClient.get(
                "MBR",
                uri -> uri.path("/api/v1/mbr/members/{memberNo}/summary").build(request.memberNo()),
                Map.class);
        return new ReferenceMemberSummaryResponse(
                text(result, "memberNo", request.memberNo()),
                text(result, "memberName", ""),
                text(result, "statusCode", "UNKNOWN"));
    }

    private String text(Map<?, ?> source, String key, String fallback) {
        Object value = source == null ? null : source.get(key);
        return value == null ? fallback : String.valueOf(value);
    }
}
