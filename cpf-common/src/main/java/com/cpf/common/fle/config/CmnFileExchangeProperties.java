package com.cpf.common.fle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** 파일 교환 저장 경로와 원격 전송 보호 정책을 바인딩합니다. */
@ConfigurationProperties(prefix = "cpf.cmn.file-exchange")
public class CmnFileExchangeProperties {
    /** 업로드·다운로드 파일을 저장할 기준 디렉터리입니다. */
    private String baseDir = "${java.io.tmpdir}/cpf-cmn-file-exchange";

    /** SSH 기반 원격 전송 기능의 활성화 여부입니다. */
    private boolean sshEnabled = false;

    /** 원격 전송을 허용할 호스트 allowlist입니다. */
    private List<String> allowedHosts = new ArrayList<>();

    /** 원격 연결과 파일 전송에 적용할 제한 시간(초)입니다. */
    private int timeoutSeconds = 15;

    /** 실제 원격 전송 없이 검증만 수행할지 여부입니다. */
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
