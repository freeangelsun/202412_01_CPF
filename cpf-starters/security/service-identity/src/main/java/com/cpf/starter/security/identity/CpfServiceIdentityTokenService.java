package com.cpf.starter.security.identity;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** HMAC service identity token with key rotation, nonce and strict expiry validation. */
public final class CpfServiceIdentityTokenService {
    private final CpfServiceIdentityProperties properties;
    private final Clock clock;
    public CpfServiceIdentityTokenService(CpfServiceIdentityProperties properties, Clock clock){this.properties=properties;this.clock=clock;}

    public String issue(String audience, String nonce){
        properties.validate();
        if(audience==null||audience.isBlank()||nonce==null||nonce.isBlank()) throw new IllegalArgumentException("audience and nonce are required");
        long issuedAt=Instant.now(clock).getEpochSecond();
        long expiresAt=issuedAt+properties.getTtl().toSeconds();
        String payload=properties.getServiceId()+"\n"+audience+"\n"+nonce+"\n"+issuedAt+"\n"+expiresAt;
        return properties.getActiveKeyId()+"."+b64(payload.getBytes(StandardCharsets.UTF_8))+"."+b64(sign(payload,properties.getActiveSecret()));
    }

    public VerifiedIdentity verify(String token, String expectedAudience){
        properties.validate();
        String[] parts=token==null?new String[0]:token.split("\\.",3);
        if(parts.length!=3) throw new SecurityException("Malformed service identity token");
        String secret=secretFor(parts[0]);
        String payload=new String(Base64.getUrlDecoder().decode(parts[1]),StandardCharsets.UTF_8);
        byte[] actual=Base64.getUrlDecoder().decode(parts[2]);
        if(!java.security.MessageDigest.isEqual(sign(payload,secret),actual)) throw new SecurityException("Invalid service identity signature");
        String[] values=payload.split("\\n",-1);
        if(values.length!=5||!values[1].equals(expectedAudience)) throw new SecurityException("Invalid service identity audience");
        long now=Instant.now(clock).getEpochSecond(); long issued=Long.parseLong(values[3]); long expires=Long.parseLong(values[4]); long skew=properties.getClockSkew().toSeconds();
        if(issued>now+skew||expires<now-skew) throw new SecurityException("Expired service identity token");
        return new VerifiedIdentity(values[0],values[1],values[2],Instant.ofEpochSecond(issued),Instant.ofEpochSecond(expires),parts[0]);
    }

    private String secretFor(String keyId){
        if(keyId.equals(properties.getActiveKeyId())) return properties.getActiveSecret();
        if(keyId.equals(properties.getPreviousKeyId())&&properties.getPreviousSecret()!=null&&!properties.getPreviousSecret().isBlank()) return properties.getPreviousSecret();
        throw new SecurityException("Unknown service identity key");
    }
    private static byte[] sign(String value,String secret){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));}catch(Exception ex){throw new IllegalStateException("Cannot sign service identity",ex);}}
    private static String b64(byte[] value){return Base64.getUrlEncoder().withoutPadding().encodeToString(value);}
    public record VerifiedIdentity(String serviceId,String audience,String nonce,Instant issuedAt,Instant expiresAt,String keyId){}
}
