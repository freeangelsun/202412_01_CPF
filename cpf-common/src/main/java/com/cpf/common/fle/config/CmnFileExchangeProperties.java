package cpf.cmn.fle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * CPF 기능 설명입니다.
 */
@ConfigurationProperties(prefix = "cpf.cmn.file-exchange")
public class CmnFileExchangeProperties {
    /**
     * CPF 기능 설명입니다.
     */
    private String baseDir = "${java.io.tmpdir}/cpf-cmn-file-exchange";

    /**
     * CPF 기능 설명입니다.
     */
    private boolean sshEnabled = false;

    /**
     * CPF 기능 설명입니다.
     */
    private List<String> allowedHosts = new ArrayList<>();

    /**
     * CPF 기능 설명입니다.
     */
    private int timeoutSeconds = 15;

    /**
     * CPF 기능 설명입니다.
     */
    private boolean dryRun = true;

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public boolean isSshEnabled() {
        return sshEnabled;
    }

    public void setSshEnabled(boolean sshEnabled) {
        this.sshEnabled = sshEnabled;
    }

    public List<String> getAllowedHosts() {
        return allowedHosts;
    }

    public void setAllowedHosts(List<String> allowedHosts) {
        this.allowedHosts = allowedHosts;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
}

