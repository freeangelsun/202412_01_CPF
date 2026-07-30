package com.cpf.admin.opr.parameter;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** ADM Reference Picker가 노출할 승인된 Secret/Path/File Metadata Catalog입니다. 원문 값과 Credential은 포함하지 않습니다. */
@ConfigurationProperties(prefix = "cpf.admin.parameter-reference")
public class AdmParameterReferenceProperties {
    private final Map<String, SecretRef> secrets = new LinkedHashMap<>();
    private final Map<String, PathAlias> paths = new LinkedHashMap<>();
    private final Map<String, FileRef> files = new LinkedHashMap<>();

    public Map<String, SecretRef> getSecrets() { return secrets; }
    public Map<String, PathAlias> getPaths() { return paths; }
    public Map<String, FileRef> getFiles() { return files; }

    public static class SecretRef {
        private String label; private String providerId; private String key; private boolean enabled = true; private String scope = "default";
        public String getLabel(){return label;} public void setLabel(String v){label=v;}
        public String getProviderId(){return providerId;} public void setProviderId(String v){providerId=v;}
        public String getKey(){return key;} public void setKey(String v){key=v;}
        public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
        public String getScope(){return scope;} public void setScope(String v){scope=v;}
    }
    public static class PathAlias {
        private String label; private String provider = "LOCAL"; private boolean enabled = true; private boolean sharedDurable; private boolean remote;
        public String getLabel(){return label;} public void setLabel(String v){label=v;}
        public String getProvider(){return provider;} public void setProvider(String v){provider=v;}
        public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
        public boolean isSharedDurable(){return sharedDurable;} public void setSharedDurable(boolean v){sharedDurable=v;}
        public boolean isRemote(){return remote;} public void setRemote(boolean v){remote=v;}
    }
    public static class FileRef {
        private String label; private String pathAlias; private String relativePath; private boolean enabled = true;
        public String getLabel(){return label;} public void setLabel(String v){label=v;}
        public String getPathAlias(){return pathAlias;} public void setPathAlias(String v){pathAlias=v;}
        public String getRelativePath(){return relativePath;} public void setRelativePath(String v){relativePath=v;}
        public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    }
}
