package com.cpf.core.api.ai;
/** 민감정보 마스킹/권한/승인 정책 hook입니다. */
public interface CpfAiPolicy { CpfAiRequest authorizeAndMask(CpfAiRequest request); default void audit(CpfAiRequest request,CpfAiResponse response,Throwable failure){} }
