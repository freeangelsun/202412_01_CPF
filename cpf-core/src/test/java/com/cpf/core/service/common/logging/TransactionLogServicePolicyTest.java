package com.cpf.core.service.common.logging;

import com.cpf.core.common.logging.TransactionLogRecord;
import com.cpf.core.common.logging.policy.LogPolicyDecision;
import com.cpf.core.common.logging.policy.LogPolicyTargetType;
import com.cpf.core.mapper.common.logging.TransactionLogMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TransactionLogServicePolicyTest {

    @Test
    void dbLogDisabledSkipsSummaryAndDetailInsert() {
        TransactionLogMapper mapper = mock(TransactionLogMapper.class);
        TransactionLogService service = new TransactionLogService(mapper);
        LogPolicyDecision policy = new LogPolicyDecision(
                LogPolicyTargetType.ONLINE_TRANSACTION.code(),
                "ADM01TRN0010",
                "INFO",
                false,
                "INFO",
                true,
                true,
                true,
                "DEFAULT",
                "ADM_OVERRIDE",
                1L,
                2L);

        service.saveTransactionLog(record(), Map.of("requestBody", "{}"), policy);

        verify(mapper, never()).insertTransactionLog(any());
        verify(mapper, never()).insertTransactionLogDetail(any(), any(), any(), any());
    }

    @Test
    void bodySavePolicyRemovesDisabledPayloadsBeforeInsert() {
        TransactionLogMapper mapper = mock(TransactionLogMapper.class);
        doAnswer(invocation -> {
            TransactionLogRecord record = invocation.getArgument(0);
            record.setLogIdx(10L);
            return null;
        }).when(mapper).insertTransactionLog(any(TransactionLogRecord.class));
        TransactionLogService service = new TransactionLogService(mapper);
        TransactionLogRecord record = record();
        Map<String, String> details = new LinkedHashMap<>();
        details.put("requestBody", "{\"password\":\"secret\"}");
        details.put("response", "{\"token\":\"secret\"}");
        details.put("error.internalMessage", "stack");
        LogPolicyDecision policy = new LogPolicyDecision(
                LogPolicyTargetType.ONLINE_TRANSACTION.code(),
                "ADM01TRN0010",
                "INFO",
                true,
                "INFO",
                false,
                false,
                false,
                "DEFAULT",
                "DB_POLICY",
                null,
                2L);

        service.saveTransactionLog(record, details, policy);

        assertThat(record.getRequestBody()).isNull();
        assertThat(record.getResponse()).isNull();
        assertThat(record.getInternalMessage()).isNull();
        assertThat(details).doesNotContainKeys("requestBody", "response", "error.internalMessage");
        verify(mapper).insertTransactionLog(record);
    }

    private TransactionLogRecord record() {
        return TransactionLogRecord.builder()
                .transactionId("20260623010101000ADMtest0010000001")
                .businessTransactionId("ADM01TRN0010")
                .requestBody("{\"request\":true}")
                .response("{\"response\":true}")
                .internalMessage("stack")
                .execUser("tester")
                .build();
    }
}
