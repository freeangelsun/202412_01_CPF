package com.cpf.batch.execution;

import org.springframework.integration.channel.DirectChannel;

/**
 * Worker handler가 호출 스레드에서 완료되어야만 send가 반환되는 제품 계약입니다.
 * ExecutorChannel 또는 비동기 MessageChannel로 교체할 수 없도록 별도 final 타입을 사용합니다.
 */
public final class CpfSynchronousWorkerChannel extends DirectChannel {
}
