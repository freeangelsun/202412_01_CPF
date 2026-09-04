package com.cpf.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ADM 최초 운영자 계정의 일회성 bootstrap 설정입니다.
 *
 * <p>모든 profile에서 같은 최초 운영자 계약을 사용한다. 비밀번호는 이 configuration object로
 * 바인딩하지 않고 runner가 canonical secret ENV에서 직접 읽으므로 YAML/property source에
 * 평문 secret이 들어올 수 없다.</p>
 */
@ConfigurationProperties(prefix = "cpf.adm.bootstrap")
public class AdmBootstrapProperties {
    private String operatorId;
    private String operatorName;

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

}
