package com.cpf.admin.opr.batch.runtime;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.batch.api.BatControlHeaders;
import com.cpf.web.api.CpfHttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BatApprovalOwnerCommandPortTest {
    private static final String BASE_URL = "http://bat-control.test";
    private static final String TRANSACTION_ID =
            "20260729170000000" + "ADM" + "admAP01" + "0000001";

    @Test
    void runtimeCommandPropagatesVerifiedApprovalAndOperatorContext() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        BatApprovalOwnerCommandPort port =
                new BatApprovalOwnerCommandPort(builder, BASE_URL, "adm-instance-01");
        AdmApprovedOperationCommand command = command("DRAIN", "runtime-01");

        server.expect(requestTo(BASE_URL + "/api/v1/batch/runtime/commands"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(BatControlHeaders.CALLER_SERVICE, "ADM"))
                .andExpect(header(BatControlHeaders.CALLER_INSTANCE_ID, "adm-instance-01"))
                .andExpect(header(BatControlHeaders.OPERATOR_ID, "approver-b"))
                .andExpect(header(CpfHttpHeaders.transactionId(), TRANSACTION_ID))
                .andExpect(header(BatControlHeaders.APPROVAL_REQUEST_ID, "42"))
                .andExpect(header(BatControlHeaders.APPROVAL_REQUESTER_ID, "requester-a"))
                .andExpect(jsonPath("$.requestedBy").value("requester-a"))
                .andExpect(jsonPath("$.approvedBy").value("approver-b"))
                .andExpect(jsonPath("$.approvalRequestId").value("42"))
                .andExpect(jsonPath("$.reason").value("approved maintenance"))
                .andRespond(withSuccess(
                        "{\"command_state\":\"SUCCEEDED\"}",
                        MediaType.APPLICATION_JSON));

        AdmApprovedOperationResult result = port.execute(command);

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        server.verify();
    }

    @Test
    void deploymentCommandUsesTheSameApprovalHeaderAndBodyActors() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        BatApprovalOwnerCommandPort port =
                new BatApprovalOwnerCommandPort(builder, BASE_URL, "adm-instance-01");
        AdmApprovedOperationCommand command = command("DEPLOY_PLAN", "plan-01");

        server.expect(requestTo(
                        BASE_URL + "/api/v1/batch/deployment-plans/plan-01/execute-approved"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(BatControlHeaders.OPERATOR_ID, "approver-b"))
                .andExpect(header(BatControlHeaders.APPROVAL_REQUEST_ID, "42"))
                .andExpect(header(BatControlHeaders.APPROVAL_REQUESTER_ID, "requester-a"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.requestedBy").value("requester-a"))
                .andExpect(jsonPath("$.approvedBy").value("approver-b"))
                .andExpect(jsonPath("$.reason").value("approved maintenance"))
                .andRespond(withSuccess(
                        """
                                {
                                  "deploymentId": "plan-01",
                                  "state": "SUCCEEDED",
                                  "message": "deployed",
                                  "instances": []
                                }
                                """,
                        MediaType.APPLICATION_JSON));

        AdmApprovedOperationResult result = port.execute(command);

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        server.verify();
    }

    private AdmApprovedOperationCommand command(String ownerCommand, String targetId) {
        return new AdmApprovedOperationCommand(
                42L,
                "ADM-APP-42-command",
                ownerCommand,
                "BAT",
                ownerCommand,
                java.util.Set.of("DEPLOY_PLAN", "ROLLBACK_PLAN").contains(ownerCommand) ? "BAT_DEPLOYMENT_PLAN" : "BAT_INSTANCE",
                targetId,
                "a".repeat(64),
                "requester-a",
                "approver-b",
                "approved maintenance",
                TRANSACTION_ID);
    }
    @Test
    void ownerTupleIsCaseSensitiveAndRejectsNearMatches() {
        BatApprovalOwnerCommandPort port =
                new BatApprovalOwnerCommandPort(RestClient.builder(), BASE_URL, "adm-instance-01");
        assertThat(port.supports("BAT", "DRAIN", "DRAIN", "BAT_INSTANCE")).isTrue();
        assertThat(port.supports("bat", "DRAIN", "DRAIN", "BAT_INSTANCE")).isFalse();
        assertThat(port.supports("BAT", "drain", "DRAIN", "BAT_INSTANCE")).isFalse();
        assertThat(port.supports("BAT", "DRAIN", "drain", "BAT_INSTANCE")).isFalse();
        assertThat(port.supports("BAT", "DRAIN", "DRAIN", "bat_instance")).isFalse();
    }

    @Test
    void riskyRemoteOwnerRejectsLoopbackAndDefaultIdentity() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new BatApprovalOwnerCommandPort(RestClient.builder(), "http://127.0.0.1:8180", "adm-instance-01"));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new BatApprovalOwnerCommandPort(RestClient.builder(), BASE_URL, "adm-local-01"));
    }

}
