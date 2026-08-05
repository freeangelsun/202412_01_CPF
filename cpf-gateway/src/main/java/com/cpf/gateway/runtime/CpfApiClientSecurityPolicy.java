package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayPrincipal;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/** 원문 key를 저장하지 않는 Gateway API Client 인증·quota 정책입니다. */
public final class CpfApiClientSecurityPolicy {
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>(Snapshot.empty());
    private final Clock clock;
    public CpfApiClientSecurityPolicy(){this(Clock.systemUTC());} CpfApiClientSecurityPolicy(Clock clock){this.clock=clock;}

    public Snapshot replace(long version, Map<String,Client> clients){
        LinkedHashMap<String,Client> normalized=new LinkedHashMap<>();
        if(clients!=null)clients.forEach((id,client)->{if(client==null)throw new IllegalArgumentException("null API client");String key=id==null||id.isBlank()?client.clientId():id;Client n=client.normalize(key);if(normalized.putIfAbsent(n.clientId(),n)!=null)throw new IllegalArgumentException("API client ID 중복");});
        if (normalized.values().stream().anyMatch(client -> client.quotaPermits() > 0)) {
            throw new IllegalArgumentException(
                    "API client quota는 RATE_LIMIT client policy로 이관해야 합니다.");
        }
        LinkedHashSet<String> keyHashes = new LinkedHashSet<>();
        for (Client client : normalized.values()) {
            if (!keyHashes.add(client.keyHash())) {
                throw new IllegalArgumentException("API client keyHash 중복");
            }
        }
        Snapshot next=new Snapshot(version,Map.copyOf(normalized));
        for (;;) {
            Snapshot current = snapshot.get();
            if (next.version() < current.version()) {
                throw new IllegalStateException("API client stale version: " + next.version());
            }
            if (next.version() == current.version() && !next.equals(current)) {
                throw new IllegalStateException("API client same-version payload conflict");
            }
            if (next.equals(current) || snapshot.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    public Snapshot snapshot() { return snapshot.get(); }

    public CpfGatewayPrincipal authenticate(String apiKey,String clientIp,String certificateSerial){
        if(apiKey==null||apiKey.isBlank())return CpfGatewayPrincipal.anonymous();
        String hash=sha256(apiKey.trim());Instant now=Instant.now(clock);
        Client client=snapshot.get().clients().values().stream().filter(c->constantEquals(c.keyHash(),hash)).findFirst().orElse(null);
        if(client==null||!client.active()||(client.expiresAt()!=null&&!now.isBefore(client.expiresAt())))return CpfGatewayPrincipal.anonymous();
        if(!client.allowedIpCidrs().isEmpty()&&client.allowedIpCidrs().stream().noneMatch(c->matchesIp(c,clientIp)))return CpfGatewayPrincipal.anonymous();
        if(!client.certificateSerials().isEmpty()&&!client.certificateSerials().contains(normalizeSerial(certificateSerial)))return CpfGatewayPrincipal.anonymous();
        return new CpfGatewayPrincipal(true,client.clientId(),client.authorities(),Map.of(
                "authType", "API_KEY",
                "clientId", client.clientId(),
                "quotaScope", "CPF_GATEWAY_RATE_LIMIT"));
    }

    private boolean matchesIp(String cidr,String ip){try{if(ip==null||ip.isBlank())return false;String[]p=cidr.split("/",2);byte[]a=InetAddress.getByName(p[0]).getAddress(),b=InetAddress.getByName(ip).getAddress();if(a.length!=b.length)return false;int bits=p.length==1?a.length*8:Integer.parseInt(p[1]);if(bits<0||bits>a.length*8)return false;for(int i=0;i<a.length;i++){int mask=bits>=8?255:bits<=0?0:(255<<(8-bits))&255;if((a[i]&mask)!=(b[i]&mask))return false;bits-=8;}return true;}catch(Exception e){return false;}}
    private static String sha256(String v){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
    private static boolean constantEquals(String a,String b){if(a==null||b==null||a.length()!=b.length())return false;int x=0;for(int i=0;i<a.length();i++)x|=a.charAt(i)^b.charAt(i);return x==0;}
    private static String normalizeSerial(String v){return v==null?"":v.replace(":","").trim().toUpperCase(Locale.ROOT);}

    public record Client(String clientId,String keyHash,boolean active,Set<String>allowedIpCidrs,Set<String>certificateSerials,Instant expiresAt,int quotaPermits,long quotaWindowMillis,Set<String>authorities){
        public Client{allowedIpCidrs=clean(allowedIpCidrs,false);certificateSerials=clean(certificateSerials,true);authorities=clean(authorities,false);if(quotaPermits<0||quotaWindowMillis<1000||quotaWindowMillis>86400000)throw new IllegalArgumentException("API client quota 범위 오류");}
        private Client normalize(String id){String normalized=id==null?"":id.trim();if(normalized.isBlank()||keyHash==null||!keyHash.matches("[0-9a-fA-F]{64}"))throw new IllegalArgumentException("clientId/keyHash 필수");return new Client(normalized,keyHash.toLowerCase(Locale.ROOT),active,allowedIpCidrs,certificateSerials,expiresAt,quotaPermits,quotaWindowMillis,authorities);}
        private static Set<String>clean(Set<String>s,boolean serial){if(s==null)return Set.of();LinkedHashSet<String>r=new LinkedHashSet<>();for(String v:s)if(v!=null&&!v.isBlank())r.add(serial?normalizeSerial(v):v.trim());return Set.copyOf(r);}
    }
    public record Snapshot(long version,Map<String,Client>clients){public Snapshot{if(version<0)throw new IllegalArgumentException("API client version must not be negative");clients=clients==null?Map.of():Map.copyOf(clients);}private static Snapshot empty(){return new Snapshot(0,Map.of());}}
}
