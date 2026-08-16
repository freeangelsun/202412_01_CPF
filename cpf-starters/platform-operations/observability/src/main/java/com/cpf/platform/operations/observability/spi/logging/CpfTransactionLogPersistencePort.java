package com.cpf.platform.operations.observability.spi.logging;
import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
/** CPF 거래 로그 projection의 topology-independent 영속 SPI. 구현체는 secret/raw payload를 저장하지 않고 재시도/복구 멱등성을 보장해야 한다. */
public interface CpfTransactionLogPersistencePort {
 /** 복구 이벤트 존재 여부를 조회한다. @param recoveryEventId 비-null 복구 이벤트 ID @return 존재하면 true @throws RuntimeException 저장소 조회 실패 시. 읽기 전용이며 side effect가 없어야 한다. */ boolean existsRecoveryEvent(String recoveryEventId);
 /** 거래 로그를 저장한다. @param record 비-null 마스킹 완료 로그 @throws RuntimeException 저장 실패 시. 호출 트랜잭션 참여 여부는 구현체가 문서화해야 하며 중복 재시도에 안전해야 한다. */ void insertTransactionLog(TransactionLogRecord record);
 /** 로그 상세를 저장한다. @param logIdx 부모 로그 키 @param detailKey 상세 키 @param detailValue 마스킹된 값 @param auditUser 감사 사용자 @throws RuntimeException 저장 실패 시. null/default는 구현 계약에 따라 거부해야 한다. */ void insertTransactionLogDetail(Long logIdx,String detailKey,String detailValue,String auditUser);
}
