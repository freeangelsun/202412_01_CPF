package cpf.adm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ADM 최초 운영자 계정의 일회성 bootstrap 설정입니다.
 *
 * <p>기본값은 비활성화이며 비밀번호는 환경변수나 secret provider가 주입해야 합니다.
 * 운영 profile은 명시적인 추가 승인 없이는 bootstrap을 실행하지 않습니다.</p>
 */
@ConfigurationProperties(prefix = "cpf.adm.bootstrap")
public class AdmBootstrapProperties {
    private boolean enabled;
    private boolean allowProd;
    private String operatorId = "admin";
    private String operatorName = "CPF 관리자";
    private String password;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAllowProd() {
        return allowProd;
    }

    public void setAllowProd(boolean allowProd) {
        this.allowProd = allowProd;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
