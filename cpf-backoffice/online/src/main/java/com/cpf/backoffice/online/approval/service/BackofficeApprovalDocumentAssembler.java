package com.cpf.backoffice.online.approval.service;

import com.cpf.backoffice.online.base.BackofficeBaseService;

import com.cpf.core.api.error.CpfValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.foundation.annotation.CpfService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * MBW 결재자가 승인 전에 확인하는 업무 판단 문서를 조립합니다.
 *
 * <p>원본 payload_json을 그대로 외부에 노출하지 않고, Snapshot hash 무결성을 검증한 뒤
 * 민감 필드를 field-level masking하여 업무별 판단 Section과 Before/After를 제공합니다.</p>
 */
// CPF stereotype 이 붙은 Business Type 은 proxy-safe 여야 한다.
// CpfCapabilityUsageAspect.proxySafeBusinessType() 이 final Type 을 proxy-unsafe 로 판정하고,
// Advisor 가 매칭되면 CGLIB subclass 생성이 불가능해 Runtime 기동이 실패한다.
@CpfService
public class BackofficeApprovalDocumentAssembler extends BackofficeBaseService {
    private static final Pattern SENSITIVE = Pattern.compile(
            "(?i).*(password|passwd|pwd|secret|token|credential|api.?key|private.?key|resident|ssn|mobile|phone|email).*");
    private final ObjectMapper mapper;

    public BackofficeApprovalDocumentAssembler(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public Map<String,Object> assemble(Map<String,Object> document, List<Map<String,Object>> history) {
        String payloadJson = Objects.toString(document.get("payloadJson"), "{}");
        String storedHash = Objects.toString(document.get("payloadHash"), "");
        String actualHash = sha256(payloadJson);
        if (storedHash.isBlank() || !constantTimeEquals(storedHash, actualHash)) {
            throw new CpfValidationException("결재 Snapshot 무결성 검증에 실패하여 승인할 수 없습니다.");
        }
        LinkedHashMap<String,Object> payload = parse(payloadJson);
        String approvalType = Objects.toString(document.get("approvalType"), "GENERIC").trim().toUpperCase(Locale.ROOT);
        String businessDomain = Objects.toString(document.get("businessDomain"), "").trim().toUpperCase(Locale.ROOT);

        LinkedHashMap<String,Object> result = new LinkedHashMap<>();
        result.put("documentType", approvalType);
        result.put("businessDomain", businessDomain);
        result.put("documentVersion", number(document.get("versionNo")));
        result.put("snapshotHash", storedHash);
        result.put("judgementReady", Boolean.TRUE);
        result.put("summary", summary(document));
        result.put("sections", sections(approvalType, payload));
        result.put("changes", changes(payload));
        result.put("history", history == null ? List.of() : List.copyOf(history));
        Object attachmentGroupId = document.get("attachmentGroupId");
        if (attachmentGroupId != null && !String.valueOf(attachmentGroupId).isBlank()) {
            result.put("attachment", Map.of("attachmentGroupId", String.valueOf(attachmentGroupId)));
        }
        return result;
    }

    private Map<String,Object> summary(Map<String,Object> document) {
        LinkedHashMap<String,Object> summary = new LinkedHashMap<>();
        for (String key : List.of("approvalId","approvalNo","approvalType","businessDomain","title",
                "requesterEmployeeNo","approvalStatus","approvalMode","currentStepNo","dueAt",
                "transactionId","versionNo","payloadHash")) {
            if (document.containsKey(key)) summary.put(key, document.get(key));
        }
        return summary;
    }

    private List<Map<String,Object>> sections(String approvalType, Map<String,Object> payload) {
        String title = switch (approvalType) {
            case "EMPLOYEE_SAVE", "BACKOFFICE_EMPLOYEE_SAVE" -> "직원 정보 변경";
            case "ORGANIZATION_SAVE", "BACKOFFICE_ORGANIZATION_SAVE" -> "조직 정보 변경";
            case "ROLE_SAVE", "AUTHORIZATION_CHANGE" -> "권한/Role 변경";
            default -> "업무 요청 상세";
        };
        LinkedHashMap<String,Object> section = new LinkedHashMap<>();
        section.put("sectionCode", approvalType);
        section.put("title", title);
        section.put("fields", maskMap(payload));
        return List.of(section);
    }

    private List<Map<String,Object>> changes(Map<String,Object> payload) {
        Object beforeRaw = payload.get("before");
        Object afterRaw = payload.get("after");
        if (!(beforeRaw instanceof Map<?,?> before) || !(afterRaw instanceof Map<?,?> after)) {
            Object changeSet = payload.get("changeSet");
            if (changeSet instanceof Collection<?> collection) {
                List<Map<String,Object>> rows = new ArrayList<>();
                for (Object item : collection) {
                    if (item instanceof Map<?,?> row) rows.add(maskMap(row));
                }
                return List.copyOf(rows);
            }
            return List.of();
        }
        TreeSet<String> keys = new TreeSet<>();
        before.keySet().forEach(k -> keys.add(String.valueOf(k)));
        after.keySet().forEach(k -> keys.add(String.valueOf(k)));
        List<Map<String,Object>> rows = new ArrayList<>();
        for (String key : keys) {
            Object b = value(before, key);
            Object a = value(after, key);
            LinkedHashMap<String,Object> row = new LinkedHashMap<>();
            row.put("field", key);
            row.put("before", mask(key, b));
            row.put("after", mask(key, a));
            row.put("changed", !Objects.deepEquals(b, a));
            rows.add(row);
        }
        return List.copyOf(rows);
    }

    private LinkedHashMap<String,Object> parse(String json) {
        try {
            if (json == null || json.isBlank()) return new LinkedHashMap<>();
            Object parsed = mapper.readValue(json, Object.class);
            if (!(parsed instanceof Map<?,?> map)) {
                throw new CpfValidationException("결재 payload는 JSON Object여야 합니다.");
            }
            LinkedHashMap<String,Object> result = new LinkedHashMap<>();
            map.forEach((k,v) -> result.put(String.valueOf(k), v));
            return result;
        } catch (CpfValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new CpfValidationException("결재 payload를 해석할 수 없어 승인할 수 없습니다.");
        }
    }

    private LinkedHashMap<String,Object> maskMap(Map<?,?> source) {
        LinkedHashMap<String,Object> target = new LinkedHashMap<>();
        source.forEach((key,value) -> {
            String name = String.valueOf(key);
            target.put(name, mask(name, value));
        });
        return target;
    }

    private Object mask(String key, Object value) {
        if (value == null) return null;
        if (SENSITIVE.matcher(key).matches()) return "***";
        if (value instanceof Map<?,?> map) return maskMap(map);
        if (value instanceof Collection<?> list) {
            List<Object> masked = new ArrayList<>(list.size());
            for (Object item : list) masked.add(item instanceof Map<?,?> map ? maskMap(map) : item);
            return List.copyOf(masked);
        }
        return value;
    }

    private static Object value(Map<?,?> map, String key) {
        for (Map.Entry<?,?> entry : map.entrySet()) if (key.equals(String.valueOf(entry.getKey()))) return entry.getValue();
        return null;
    }

    private static long number(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value == null) return 0L;
        return Long.parseLong(String.valueOf(value));
    }

    private static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(left.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII),
                right.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }
}
