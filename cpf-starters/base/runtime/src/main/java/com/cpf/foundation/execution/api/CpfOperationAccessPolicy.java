package com.cpf.foundation.execution.api;

/** Controller 진입 전 trusted System/Domain, Operation, 필요 시 Caller Channel 정책을 평가하는 Runtime 계약입니다. */
public interface CpfOperationAccessPolicy {
    Decision evaluate(Request request);

    /**
     * System/Domain 정책용 trusted identity와 거래 Channel identity를 분리합니다.
     * callerSystemCode/targetSystemCode는 Header를 재해석하지 않고 Trust/Runtime Registry에서 확정한 값이며,
     * callerChannel은 Canonical Channel Policy 입력입니다.
     */
    record Request(
            String operationId,
            String callerSystemCode,
            String targetSystemCode,
            String callerChannel,
            boolean authenticated,
            boolean signed,
            boolean trustedInternal) {
        public Request {
            operationId = required(operationId, "operationId");
            targetSystemCode = required(targetSystemCode, "targetSystemCode");
            callerSystemCode = optional(callerSystemCode);
            callerChannel = optional(callerChannel);
        }

        /** Request 동작은 Controller 진입 전에 trusted System/Domain·Operation·Caller Channel 정책을 평가하는 Runtime 계약에서 필요한 공개 동작을 수행합니다. */
        public Request(String operationId, String callerSystemCode, String targetSystemCode,
                String callerChannel, boolean trustedInternal) {
            this(operationId, callerSystemCode, targetSystemCode, callerChannel, false, false, trustedInternal);
        }

        private static String required(String v, String n) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(n + " is required");
            return v.trim();
        }
        private static String optional(String v) { return v == null || v.isBlank() ? null : v.trim(); }
    }

    record Decision(boolean allowed, String reasonCode, long policyVersion) {
        public Decision { reasonCode = reasonCode == null || reasonCode.isBlank() ? (allowed ? "ALLOW" : "DENY") : reasonCode.trim(); }
        /** deny 동작은 Controller 진입 전에 trusted System/Domain·Operation·Caller Channel 정책을 평가하는 Runtime 계약에서 필요한 공개 동작을 수행합니다. */
        public static Decision deny(String reason,long version){return new Decision(false,reason,version);}
        /** allow 동작은 Controller 진입 전에 trusted System/Domain·Operation·Caller Channel 정책을 평가하는 Runtime 계약에서 필요한 공개 동작을 수행합니다. */
        public static Decision allow(long version){return new Decision(true,"ALLOW",version);}
    }
}
