package com.cpf.starter.logging;

import com.cpf.common.logging.CpfLogFilePolicy;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 각 실행 Application의 visible YML에서 바인딩하는 일반 File Logging 설정입니다. */
@ConfigurationProperties("cpf.logging")
public final class CpfApplicationLoggingProperties {
    private boolean enabled = true;
    private Path root = Path.of("logs");
    private String instanceId;
    private Duration maintenanceInterval = Duration.ofHours(1);
    private Map<String, FilePolicy> files = new LinkedHashMap<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Path getRoot() { return root; }
    public void setRoot(Path root) { this.root = root; }
    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
    public Duration getMaintenanceInterval() { return maintenanceInterval; }
    public void setMaintenanceInterval(Duration maintenanceInterval) {
        this.maintenanceInterval = maintenanceInterval;
    }
    public Map<String, FilePolicy> getFiles() { return files; }
    public void setFiles(Map<String, FilePolicy> files) {
        this.files = files == null ? new LinkedHashMap<>() : new LinkedHashMap<>(files);
    }

    /** 논리 로그파일별로 독립 적용되는 공통 schema입니다. */
    public static final class FilePolicy {
        private boolean enabled = true;
        private String fileName;
        private String level;
        private CpfLogFilePolicy.Rolling rolling = CpfLogFilePolicy.Rolling.DAILY;
        private int compressAfterDays = 5;
        private int deleteAfterDays = 365;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public CpfLogFilePolicy.Rolling getRolling() { return rolling; }
        public void setRolling(CpfLogFilePolicy.Rolling rolling) { this.rolling = rolling; }
        public int getCompressAfterDays() { return compressAfterDays; }
        public void setCompressAfterDays(int compressAfterDays) {
            this.compressAfterDays = compressAfterDays;
        }
        public int getDeleteAfterDays() { return deleteAfterDays; }
        public void setDeleteAfterDays(int deleteAfterDays) { this.deleteAfterDays = deleteAfterDays; }

        CpfLogFilePolicy toPolicy() {
            return new CpfLogFilePolicy(
                    enabled, fileName, level, rolling, compressAfterDays, deleteAfterDays);
        }
    }
}
