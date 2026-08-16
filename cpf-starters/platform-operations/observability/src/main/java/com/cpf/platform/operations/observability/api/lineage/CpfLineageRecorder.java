/** CpfLineageRecorder 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
package com.cpf.platform.operations.observability.api.lineage;public interface CpfLineageRecorder { void record(CpfLineageRecord record); }
