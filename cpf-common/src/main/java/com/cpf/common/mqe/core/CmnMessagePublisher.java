package com.cpf.common.mqe.core;

import java.util.Map;

/** 브로커 종류와 무관하게 메시지를 발행하는 CMN 공통 계약입니다. */
public interface CmnMessagePublisher {

    /** 기본 목적지로 메시지를 발행합니다. */
    CmnMessagePublishResult publish(String key, Object payload);

    /** 목적지·메시지 키·전파 헤더를 지정해 메시지를 발행합니다. */
    CmnMessagePublishResult publish(String destination, String key, Object payload, Map<String, String> headers);
}

