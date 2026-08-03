package com.cpf.common.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** CPF Redis Adapter의 topology/security/fallback 정책입니다. */
@ConfigurationProperties("cpf.data.cache.caffeine")
public class CpfRedisProperties {
    private Provider provider = Provider.LOCAL;
    private Topology topology = Topology.STANDALONE;
    private List<String> nodes = new ArrayList<>(List.of("127.0.0.1:6379"));
    private String master = "mymaster";
    private int database;
    private String username = "";
    private String secretReference = "";
    private boolean tls;
    private boolean production;
    private boolean allowLocalSimulator = true;
    private boolean failOpen = false;
    private boolean allowLoopback = false;
    private Duration commandTimeout = Duration.ofSeconds(2);
    private Duration shutdownTimeout = Duration.ofSeconds(2);
    private String invalidationChannel = "cpf:cache:invalidate";
    private String consumerId = "";
    private int reconcileBatchSize = 200;

    public enum Provider { LOCAL, REDIS }
    public enum Topology { STANDALONE, SENTINEL, CLUSTER }

    public void validate() { validate(false); }

    public void validate(boolean productionProfile) {
        boolean effectiveProduction = production || productionProfile;
        if (effectiveProduction && provider == Provider.LOCAL) throw new IllegalStateException("운영 Profile에서 Local Cache Provider를 사용할 수 없습니다.");
        if (provider == Provider.LOCAL && !allowLocalSimulator) throw new IllegalStateException("Local Cache Simulator가 비활성화되었습니다.");
        if (provider == Provider.REDIS && nodes.isEmpty()) throw new IllegalStateException("Redis node는 1개 이상 필요합니다.");
        if (provider == Provider.REDIS) nodes.forEach(this::validateNode);
        if (provider == Provider.REDIS && effectiveProduction && !tls) throw new IllegalStateException("운영 Redis는 TLS가 필수입니다.");
        if (provider == Provider.REDIS && effectiveProduction && secretReference.isBlank()) throw new IllegalStateException("운영 Redis는 Secret Reference가 필수입니다.");
        if (provider == Provider.REDIS && effectiveProduction && consumerId.isBlank()) throw new IllegalStateException("운영 Redis는 안정적인 consumer-id가 필수입니다.");
        if (provider == Provider.REDIS && effectiveProduction && !allowLoopback
                && nodes.stream().anyMatch(this::isLoopbackNode)) {
            throw new IllegalStateException("운영 Redis에서 loopback node를 사용할 수 없습니다.");
        }
        if (topology == Topology.SENTINEL && (master == null || master.isBlank())) throw new IllegalStateException("Sentinel master 이름은 필수입니다.");
        if (topology == Topology.STANDALONE && nodes.size() != 1) throw new IllegalStateException("Standalone topology는 node 1개만 허용합니다.");
        if (database < 0 || database > 15) throw new IllegalStateException("Redis database는 0~15 범위여야 합니다.");
        if (topology == Topology.CLUSTER && database != 0) throw new IllegalStateException("Redis Cluster는 database 0만 지원합니다.");
        if (!consumerId.isBlank() && !consumerId.matches("[A-Za-z0-9._:-]{1,180}")) throw new IllegalStateException("Redis consumer-id 형식이 올바르지 않습니다.");
        if (commandTimeout == null || commandTimeout.isZero() || commandTimeout.isNegative())
            throw new IllegalStateException("Redis commandTimeout은 0보다 커야 합니다.");
        if (shutdownTimeout == null || shutdownTimeout.isNegative())
            throw new IllegalStateException("Redis shutdownTimeout은 0 이상이어야 합니다.");
        if (provider == Provider.REDIS && (invalidationChannel == null || invalidationChannel.isBlank()))
            throw new IllegalStateException("Redis invalidationChannel은 필수입니다.");
        if (reconcileBatchSize < 1 || reconcileBatchSize > 2000) throw new IllegalStateException("reconcileBatchSize는 1~2000이어야 합니다.");
    }
    public Provider getProvider(){return provider;} public void setProvider(Provider v){provider=v;}
    public Topology getTopology(){return topology;} public void setTopology(Topology v){topology=v;}
    public List<String> getNodes(){return nodes;} public void setNodes(List<String> v){nodes=v==null?new ArrayList<>():new ArrayList<>(v);}
    public String getMaster(){return master;} public void setMaster(String v){master=v;}
    public int getDatabase(){return database;} public void setDatabase(int v){database=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v==null?"":v.trim();}
    public String getSecretReference(){return secretReference;} public void setSecretReference(String v){secretReference=v==null?"":v.trim();}
    public boolean isTls(){return tls;} public void setTls(boolean v){tls=v;}
    public boolean isProduction(){return production;} public void setProduction(boolean v){production=v;}
    public boolean isAllowLocalSimulator(){return allowLocalSimulator;} public void setAllowLocalSimulator(boolean v){allowLocalSimulator=v;}
    public boolean isFailOpen(){return failOpen;} public void setFailOpen(boolean v){failOpen=v;}
    public boolean isAllowLoopback(){return allowLoopback;} public void setAllowLoopback(boolean v){allowLoopback=v;}
    public Duration getCommandTimeout(){return commandTimeout;} public void setCommandTimeout(Duration v){commandTimeout=v;}
    public Duration getShutdownTimeout(){return shutdownTimeout;} public void setShutdownTimeout(Duration v){shutdownTimeout=v;}
    public String getInvalidationChannel(){return invalidationChannel;} public void setInvalidationChannel(String v){invalidationChannel=v;}
    public String getConsumerId(){return consumerId;} public void setConsumerId(String v){consumerId=v==null?"":v;}
    public int getReconcileBatchSize(){return reconcileBatchSize;} public void setReconcileBatchSize(int v){reconcileBatchSize=v;}

    private void validateNode(String value) {
        if (value == null || value.isBlank()) throw new IllegalStateException("Redis node는 host:port 형식이어야 합니다.");
        String node=value.trim();
        int separator=node.startsWith("[") ? node.indexOf("]:") + 1 : node.lastIndexOf(':');
        if (separator <= 0 || separator >= node.length()-1) throw new IllegalStateException("Redis node는 host:port 형식이어야 합니다.");
        String host=node.startsWith("[") ? node.substring(1,separator-1) : node.substring(0,separator);
        if (host.isBlank() || host.chars().anyMatch(Character::isWhitespace)) throw new IllegalStateException("Redis host 형식이 올바르지 않습니다.");
        try {
            int port=Integer.parseInt(node.substring(separator+1));
            if (port<1 || port>65535) throw new IllegalStateException("Redis port 범위가 올바르지 않습니다.");
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Redis port 형식이 올바르지 않습니다.",ex);
        }
    }

    private boolean isLoopbackNode(String value) {
        String node=value.trim();
        int separator=node.startsWith("[") ? node.indexOf("]:") + 1 : node.lastIndexOf(':');
        String host=node.startsWith("[") ? node.substring(1,separator-1) : node.substring(0,separator);
        String normalized=host.toLowerCase(Locale.ROOT);
        return normalized.equals("localhost") || normalized.equals("127.0.0.1")
                || normalized.equals("0.0.0.0") || normalized.equals("::1");
    }
}
