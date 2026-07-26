package com.cpf.batch.worker;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

/** 승인 Shell/File Executor의 고정 Catalog. Runtime 요청으로 임의 Path/Command를 주입할 수 없습니다. */
@ConfigurationProperties("cpf.batch.worker.operations")
public class WorkerOperationalProperties {
    private Map<String, ShellDefinition> scripts = new LinkedHashMap<>();
    private Map<String, PathAlias> pathAliases = new LinkedHashMap<>();

    public Map<String, ShellDefinition> getScripts() { return scripts; }
    public void setScripts(Map<String, ShellDefinition> scripts) { this.scripts = scripts == null ? new LinkedHashMap<>() : scripts; }
    public Map<String, PathAlias> getPathAliases() { return pathAliases; }
    public void setPathAliases(Map<String, PathAlias> pathAliases) { this.pathAliases = pathAliases == null ? new LinkedHashMap<>() : pathAliases; }

    public static class ShellDefinition {
        private String executable;
        private List<String> fixedArguments = new ArrayList<>();
        private List<String> allowedParameters = new ArrayList<>();
        private int timeoutSeconds = 600;
        public String getExecutable() { return executable; }
        public void setExecutable(String executable) { this.executable = executable; }
        public List<String> getFixedArguments() { return fixedArguments; }
        public void setFixedArguments(List<String> fixedArguments) { this.fixedArguments = fixedArguments == null ? new ArrayList<>() : fixedArguments; }
        public List<String> getAllowedParameters() { return allowedParameters; }
        public void setAllowedParameters(List<String> allowedParameters) { this.allowedParameters = allowedParameters == null ? new ArrayList<>() : allowedParameters; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }

    public static class PathAlias {
        private String root;
        private boolean sharedDurable;
        public String getRoot() { return root; }
        public void setRoot(String root) { this.root = root; }
        public boolean isSharedDurable() { return sharedDurable; }
        public void setSharedDurable(boolean sharedDurable) { this.sharedDurable = sharedDurable; }
    }
}
