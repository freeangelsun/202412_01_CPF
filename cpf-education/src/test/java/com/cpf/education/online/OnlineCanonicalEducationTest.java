package com.cpf.education.online;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.messaging.api.CpfMessageListener;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import static org.assertj.core.api.Assertions.assertThat;

class OnlineCanonicalEducationTest {
    @Test
    void exactlyTwentyOnlineExamplesUseOneCanonicalOperationId() throws Exception {
        for (int i = 1; i <= 20; i++) {
            Class<?> type = Class.forName("com.cpf.education.online.Online" + String.format("%02d", i) + suffix(i));
            List<String> cpfIds = new ArrayList<>();
            List<String> openApiIds = new ArrayList<>();
            for (Method method : type.getDeclaredMethods()) {
                CpfOnlineTransaction cpf = method.getAnnotation(CpfOnlineTransaction.class);
                Operation api = method.getAnnotation(Operation.class);
                if (cpf != null) cpfIds.add(cpf.operationId());
                if (api != null && !api.operationId().isBlank()) openApiIds.add(api.operationId());
            }
            String expected = String.format("EDU-ONLINE-%02d", i);
            assertThat(cpfIds).containsExactly(expected);
            assertThat(openApiIds).contains(expected);
        }
    }

    @Test
    void criticalGoldenPathsAreNotReducedToMarkerSamples() throws Exception {
        assertThat(Online01BasicCrudExample.MemberService.class.getDeclaredMethod(
                "update", Online01BasicCrudExample.CrudCommand.class)).isNotNull();
        assertThat(Online02SearchPagingExample.class.getDeclaredMethod(
                "search", Online02SearchPagingExample.SearchMode.class, String.class, String.class, int.class, int.class)).isNotNull();

        Method requiredChild = Online09RequiredTransactionExample.ChildService.class
                .getDeclaredMethod("saveChild", Online09RequiredTransactionExample.Command.class);
        assertThat(requiredChild.getAnnotation(CpfTransactional.class).propagation()).isEqualTo(Propagation.REQUIRED);

        Method requiresNew = Online10RequiresNewTransactionExample.IndependentAuditService.class
                .getDeclaredMethod("writeAudit", String.class);
        assertThat(requiresNew.getAnnotation(CpfTransactional.class).propagation()).isEqualTo(Propagation.REQUIRES_NEW);

        Method listener = Online15MessagingExample.MemberChangedConsumer.class
                .getDeclaredMethod("consume", com.cpf.messaging.api.CpfBrokerBridgeMessage.class);
        assertThat(listener.getAnnotation(CpfMessageListener.class)).isNotNull();

        assertThat(Online16FileBulkExample.class.getDeclaredMethod(
                "uploadAndDownload", Online16FileBulkExample.Command.class)).isNotNull();
        assertThat(Online20WebhookCallbackExample.class.getDeclaredMethod(
                "callback", Online20WebhookCallbackExample.CallbackCommand.class)).isNotNull();
    }

    private static String suffix(int i) {
        return switch (i) {
            case 1 -> "BasicCrudExample"; case 2 -> "SearchPagingExample"; case 3 -> "CommonCatalogExample";
            case 4 -> "ValidationErrorExample"; case 5 -> "LocalServiceCallExample"; case 6 -> "DomainCallExample";
            case 7 -> "ExternalRestCallExample"; case 8 -> "FixedLengthExternalCallExample"; case 9 -> "RequiredTransactionExample";
            case 10 -> "RequiresNewTransactionExample"; case 11 -> "ExternalSideEffectTransactionExample"; case 12 -> "OnDemandBatchExample";
            case 13 -> "CenterCutExample"; case 14 -> "CacheExample"; case 15 -> "MessagingExample"; case 16 -> "FileBulkExample";
            case 17 -> "SecurityAuditExample"; case 18 -> "IdempotencyRecoveryExample"; case 19 -> "OptimisticLockExample";
            case 20 -> "WebhookCallbackExample"; default -> throw new IllegalArgumentException();
        };
    }
}
