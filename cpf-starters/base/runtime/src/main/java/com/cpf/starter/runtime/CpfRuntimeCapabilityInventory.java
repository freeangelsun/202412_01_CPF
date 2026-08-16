package com.cpf.starter.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/** Canonical Starter/Provider가 제공하는 META-INF/cpf/runtime-capability.properties를 자동 수집하는 Runtime Inventory입니다. */
public final class CpfRuntimeCapabilityInventory {
    public static final String RESOURCE = "META-INF/cpf/runtime-capability.properties";
    private final ConcurrentHashMap<String, CpfRuntimeCapabilityDescriptor> descriptors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CpfRuntimeCapabilityDescriptor> resolveCache = new ConcurrentHashMap<>();

    public static CpfRuntimeCapabilityInventory fromClasspath(ClassLoader loader) {
        CpfRuntimeCapabilityInventory inventory = new CpfRuntimeCapabilityInventory();
        inventory.load(loader == null ? CpfRuntimeCapabilityInventory.class.getClassLoader() : loader);
        return inventory;
    }

    public void register(CpfRuntimeCapabilityDescriptor descriptor) {
        CpfRuntimeCapabilityDescriptor previous = descriptors.putIfAbsent(descriptor.id(), descriptor);
        if (previous != null && !previous.equals(descriptor)) {
            throw new IllegalStateException("Conflicting CPF runtime capability descriptor: " + descriptor.id());
        }
    }

    public List<CpfRuntimeCapabilityDescriptor> all() {
        return descriptors.values().stream().sorted(java.util.Comparator.comparing(CpfRuntimeCapabilityDescriptor::id)).toList();
    }

    public List<String> capabilityIds() { return all().stream().map(CpfRuntimeCapabilityDescriptor::id).toList(); }

    /** 실행 클래스의 packageBase를 가장 길게 일치시키며 실제 사용 Capability를 자동 식별합니다. */
    public CpfRuntimeCapabilityDescriptor resolveByClassName(String className) {
        if (className == null || className.isBlank()) return null;
        CpfRuntimeCapabilityDescriptor cached = resolveCache.get(className);
        if (cached != null) return cached;
        CpfRuntimeCapabilityDescriptor found = all().stream()
                .filter(CpfRuntimeCapabilityDescriptor::operatorVisible)
                .filter(d -> { String p=d.metadata().get("packageBase"); return p!=null&&!p.isBlank()&&(className.equals(p)||className.startsWith(p+".")); })
                .max(java.util.Comparator.comparingInt(d -> d.metadata().get("packageBase").length()))
                .orElse(null);
        if (found != null) resolveCache.put(className, found);
        return found;
    }

    /** ADM health diagnostics에 넣을 bounded 문자열입니다. Secret/config value는 포함하지 않습니다. */
    public Map<String, String> publicDiagnostics() {
        Map<String,String> out = new LinkedHashMap<>();
        for (CpfRuntimeCapabilityDescriptor d : all()) {
            out.put("starter." + d.id(), String.join("|", d.starterArtifactId(), d.capability(), d.provider(),
                    d.managementCategory(), d.usageLevel(), d.runtimeRequired() ? "runtime" : "library"));
            String prefix = "starterMeta." + d.id() + ".";
            out.put(prefix + "artifactId", d.starterArtifactId());
            out.put(prefix + "capability", d.capability());
            out.put(prefix + "provider", d.provider());
            out.put(prefix + "category", d.managementCategory());
            out.put(prefix + "usageLevel", d.usageLevel());
            out.put(prefix + "runtimeRequired", Boolean.toString(d.runtimeRequired()));
            out.put(prefix + "dedicatedWorkflow", Boolean.toString(d.dedicatedWorkflow()));
            out.put(prefix + "operatorVisible", Boolean.toString(d.operatorVisible()));
            out.put(prefix + "automaticRegistration", Boolean.toString(d.automaticRegistration()));
            out.put(prefix + "managementScope", d.managementScope());
            out.put(prefix + "commonAreas", String.join(",", d.commonAreas()));
            out.put(prefix + "supports", supportText(d.support()));
        }
        return Map.copyOf(out);
    }

    private void load(ClassLoader loader) {
        try {
            Enumeration<URL> resources = loader.getResources(RESOURCE);
            List<URL> urls = Collections.list(resources);
            urls.sort(java.util.Comparator.comparing(URL::toString));
            for (URL url : urls) load(url);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to discover CPF runtime capability metadata", e);
        }
    }

    private void load(URL url) {
        Properties p = new Properties();
        try (InputStream in = url.openStream()) { p.load(in); }
        catch (IOException e) { throw new IllegalStateException("Failed to read CPF capability metadata: " + url, e); }
        Map<String,String> metadata = new LinkedHashMap<>();
        for (String key : p.stringPropertyNames()) if (key.startsWith("meta.")) metadata.put(key.substring(5), p.getProperty(key));
        register(new CpfRuntimeCapabilityDescriptor(
                p.getProperty("id"), p.getProperty("starterArtifactId"), p.getProperty("capability"),
                p.getProperty("provider", "cpf"), p.getProperty("configPrefix", ""), p.getProperty("ownerGroup"),
                Boolean.parseBoolean(p.getProperty("runtimeRequired", "false")), p.getProperty("usageLevel", "capability"),
                p.getProperty("managementCategory", "RUNTIME"), Boolean.parseBoolean(p.getProperty("dedicatedWorkflow", "false")),
                Boolean.parseBoolean(p.getProperty("operatorVisible", "false")), Boolean.parseBoolean(p.getProperty("automaticRegistration", "true")),
                p.getProperty("managementScope", "component"), splitCsv(p.getProperty("commonAreas", "OPERATIONS,LOG_TRACE,FAILURE_RECOVERY,CONFIG_POLICY,AUDIT_CHANGE")),
                new CpfRuntimeCapabilityDescriptor.Support(flag(p,"health"),flag(p,"metrics"),flag(p,"logs"),flag(p,"trace"),
                        flag(p,"effectiveConfig"),flag(p,"failure"),flag(p,"audit"),flag(p,"dynamicConfig"),
                        flag(p,"runtimeControl"),flag(p,"recovery")), metadata));
    }
    private static boolean flag(Properties p, String name) { return Boolean.parseBoolean(p.getProperty("supports." + name, "false")); }
    private static List<String> splitCsv(String value) { return Arrays.stream(value.split(",")).map(String::trim).filter(v -> !v.isEmpty()).toList(); }
    private static String supportText(CpfRuntimeCapabilityDescriptor.Support s) {
        return String.join(",",
                "health=" + s.health(), "metrics=" + s.metrics(), "logs=" + s.logs(), "trace=" + s.trace(),
                "effectiveConfig=" + s.effectiveConfig(), "failure=" + s.failure(), "audit=" + s.audit(),
                "dynamicConfig=" + s.dynamicConfig(), "runtimeControl=" + s.runtimeControl(), "recovery=" + s.recovery());
    }

}
