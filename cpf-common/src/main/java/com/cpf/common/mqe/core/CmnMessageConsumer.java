package com.cpf.common.mqe.core;

import java.util.List;

/** 메시지 구독과 최근 발행 이력 조회를 제공하는 브로커 독립 소비자 계약입니다. */
public interface CmnMessageConsumer {

    /** 목적지에 메시지 처리기를 등록합니다. */
    void subscribe(String destination, CmnMessageHandler handler);

    /** 목적지별 최근 메시지를 최신 순으로 조회합니다. */
    List<CmnMessageEnvelope> findRecentMessages(String destination, int limit);
}

