package com.cpf.core.api.ai;
/** AI provider SPI. 구현은 provider starter/업무 모듈이 소유합니다. */
public interface CpfAiProvider { String providerId(); boolean supports(String model); CpfAiResponse execute(CpfAiRequest request) throws Exception; default boolean safeToFallbackAfterTimeout(){ return false; } }
