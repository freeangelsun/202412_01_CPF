package com.cpf.core.api.logging.policy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** 저장 전 Allowlist·마스킹·Hash·Byte 상한을 적용하는 공통 Capture Guard입니다. */
public final class CpfLogCaptureGuard {
    private static final Set<String> FORBIDDEN_HEADERS = Set.of(
            "authorization","proxy-authorization","cookie","set-cookie","x-api-key","api-key");
    private static final Pattern JSON_SECRET = Pattern.compile(
            "(?i)(\\\"(?:password|passwd|secret|token|accessToken|refreshToken|authorization|residentNo|ssn|cardNo|accountNo)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")");
    private static final Pattern QUERY_SECRET = Pattern.compile(
            "(?i)(password|passwd|secret|token|authorization|residentNo|ssn|cardNo|accountNo)=([^&]*)");

    private CpfLogCaptureGuard() {}

    public static CapturedValue query(String rawQuery, LogPolicyDecision policy) {
        if (rawQuery == null || rawQuery.isBlank() || policy.queryCaptureMode() == LogCaptureMode.NONE) return CapturedValue.empty();
        String value=switch(policy.queryCaptureMode()) {
            case HASHED -> "sha256:"+sha256(rawQuery);
            case ALLOWLIST -> allowlistedQuery(rawQuery,policy.queryAllowlist());
            case MASKED -> QUERY_SECRET.matcher(rawQuery).replaceAll("$1=***");
            default -> throw new IllegalArgumentException("Query Capture Mode 오류: "+policy.queryCaptureMode());
        };
        return truncate(value,policy.maxQueryBytes());
    }

    public static CapturedValue headers(Map<String,? extends List<String>> headers, boolean response, LogPolicyDecision policy) {
        LogCaptureMode mode=response?policy.responseHeaderCaptureMode():policy.requestHeaderCaptureMode();
        if(headers==null||headers.isEmpty()||mode==LogCaptureMode.NONE) return CapturedValue.empty();
        Map<String,String> out=new LinkedHashMap<>();
        for(var e:headers.entrySet()) {
            String name=e.getKey()==null?"":e.getKey().trim().toLowerCase(Locale.ROOT);
            if(name.isBlank()||isSensitiveHeader(name)) continue;
            if(mode==LogCaptureMode.ALLOWLIST&&!policy.headerAllowlist().contains(name)) continue;
            String value=String.join(",",e.getValue()==null?List.of():e.getValue());
            out.put(name,mode==LogCaptureMode.MASKED?mask(value):value);
        }
        String text=out.entrySet().stream().map(e->e.getKey()+":"+e.getValue()).reduce((a,b)->a+"\n"+b).orElse("");
        return truncate(text,policy.maxHeaderBytes());
    }

    public static CapturedValue body(String body, boolean response, LogPolicyDecision policy,
            CpfPayloadProtectionPort protectionPort) {
        LogCaptureMode mode=response?policy.responseBodyCaptureMode():policy.requestBodyCaptureMode();
        int max=response?policy.maxResponseBodyBytes():policy.maxRequestBodyBytes();
        if(body==null||mode==LogCaptureMode.NONE) return CapturedValue.empty();
        if(mode==LogCaptureMode.METADATA_ONLY) return new CapturedValue(
                "bytes="+body.getBytes(StandardCharsets.UTF_8).length+",sha256="+sha256(body),false,true);
        if(mode==LogCaptureMode.ENCRYPTED_BODY) {
            if(protectionPort==null) throw new IllegalStateException("ENCRYPTED_BODY 보호 Port가 구성되지 않았습니다.");
            var p=protectionPort.protect(body,policy.maskingPolicyKey());
            return truncate("algorithm="+p.algorithm()+",keyRef="+p.keyReference()+",ciphertext="+p.ciphertext(),max);
        }
        String value=mode==LogCaptureMode.ALLOWLIST_FIELDS?allowlistedFields(body,policy.fieldAllowlist()):mask(body);
        return truncate(value,max);
    }

    public static CapturedValue stack(String stack,LogPolicyDecision policy) {
        if(stack==null||policy.errorStackCaptureMode()==LogCaptureMode.NONE) return CapturedValue.empty();
        String value=policy.errorStackCaptureMode()==LogCaptureMode.SUMMARY
                ? stack.lines().limit(8).reduce((a,b)->a+"\n"+b).orElse("") : mask(stack);
        return truncate(value,policy.maxStackBytes());
    }

    public static CapturedValue truncate(String value,int maxBytes) {
        if(value==null||value.isEmpty()) return CapturedValue.empty();
        byte[] bytes=value.getBytes(StandardCharsets.UTF_8);
        if(bytes.length<=maxBytes) return new CapturedValue(value,false,false);
        if(maxBytes<=0) return new CapturedValue("",true,false);
        int end=Math.min(maxBytes,bytes.length);
        // end는 포함할 마지막 Byte 다음 위치입니다. 잘린 다음 Byte가 UTF-8 continuation이면
        // 해당 문자의 lead byte 직전까지 되돌려 깨진 문자열 저장을 방지합니다.
        while(end>0 && end<bytes.length && (bytes[end]&0xC0)==0x80) end--;
        return new CapturedValue(new String(bytes,0,end,StandardCharsets.UTF_8),true,false);
    }

    public static String mask(String value) {
        if(value==null) return null;
        String masked=JSON_SECRET.matcher(value).replaceAll("$1***$2");
        masked=QUERY_SECRET.matcher(masked).replaceAll("$1=***");
        return masked.replaceAll("(?i)Bearer\\s+[A-Za-z0-9._~+/-]+=*","Bearer ***");
    }

    private static String allowlistedQuery(String raw,List<String> allowed) {
        if(allowed==null||allowed.isEmpty()) return "";
        return java.util.Arrays.stream(raw.split("&"))
                .filter(part->{int i=part.indexOf('=');String k=(i<0?part:part.substring(0,i)).toLowerCase(Locale.ROOT);return allowed.contains(k);})
                .map(part->QUERY_SECRET.matcher(part).replaceAll("$1=***"))
                .reduce((a,b)->a+"&"+b).orElse("");
    }

    private static String allowlistedFields(String body,List<String> fields) {
        if(fields==null||fields.isEmpty()) return "{}";
        // JSONPath/XPath 전문 Adapter가 없는 경우에도 자유 원문 저장으로 후퇴하지 않습니다.
        Map<String,String> found=new LinkedHashMap<>();
        for(String field:fields) {
            String key=field.replace("$.","").replace("/","").trim();
            if(key.isBlank()) continue;
            Pattern p=Pattern.compile("(?i)\\\""+Pattern.quote(key)+"\\\"\\s*:\\s*(\\\"[^\\\"]*\\\"|[-0-9.]+|true|false|null)");
            var m=p.matcher(body); if(m.find()) found.put(key,mask(m.group(1)));
        }
        return found.entrySet().stream().map(e->"\\\""+e.getKey()+"\\\":"+e.getValue()).reduce((a,b)->a+","+b).map(v->"{"+v+"}").orElse("{}");
    }

    private static boolean isSensitiveHeader(String name) {
        return FORBIDDEN_HEADERS.contains(name)||name.contains("token")||name.contains("secret")||name.contains("password");
    }
    private static String sha256(String value) {
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
        catch(Exception impossible){throw new IllegalStateException("SHA-256 unavailable",impossible);}
    }
    public record CapturedValue(String value,boolean truncated,boolean metadataOnly) {
        public static CapturedValue empty(){return new CapturedValue("",false,false);}
    }
}
