package com.cpf.foundation.execution.api;

/** Controller 진입 전 System/Domain/Operation 호출정책을 평가하는 Runtime 계약입니다. */
public interface CpfOperationAccessPolicy {
    Decision evaluate(Request request);

    record Request(String operationId, String callerSystemCode, String targetSystemCode, String channelCode, boolean trustedInternal) {
        public Request {
            operationId=required(operationId,"operationId"); targetSystemCode=required(targetSystemCode,"targetSystemCode");
            callerSystemCode=optional(callerSystemCode); channelCode=optional(channelCode);
        }
        private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
        private static String optional(String v){return v==null||v.isBlank()?null:v.trim();}
    }
    record Decision(boolean allowed, String reasonCode, long policyVersion) {
        public Decision { reasonCode=reasonCode==null||reasonCode.isBlank()?(allowed?"ALLOW":"DENY"):reasonCode.trim(); }
        /** 호출을 차단하는 Operation 접근정책 결과를 생성합니다. */
        public static Decision deny(String reason,long version){return new Decision(false,reason,version);}
        /** 호출을 허용하는 Operation 접근정책 결과를 생성합니다. */
        public static Decision allow(long version){return new Decision(true,"ALLOW",version);}
    }
}
