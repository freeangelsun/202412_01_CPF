package com.cpf.platform.operations.observability.spi.logging.lineage;
import com.cpf.platform.operations.observability.spi.logging.lineage.CpfTransactionLineageRecord;
/** 정규화된 거래 lineage projection을 영속화하는 topology-independent SPI. 구현체는 동일 lineageId 재시도를 멱등하게 처리해야 한다. */
public interface CpfTransactionLineageProjectionPort {
 /** projection을 insert/update 한다. @param record 비-null 정규화 record @throws RuntimeException 저장소 접근 실패 시. 구현체는 트랜잭션 경계를 명시하고 concurrent upsert를 안전하게 처리해야 한다. */
 void upsert(CpfTransactionLineageRecord record);
}
