package com.cpf.security.secret;

import com.cpf.security.api.audit.CpfTamperAuditOperations;
import com.cpf.security.api.audit.CpfTamperAuditRecord;
import com.cpf.security.api.audit.CpfTamperAuditStore;
import com.cpf.security.api.crypto.CpfDigitalSignatureOperations;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;

/** SHA-256 hash-chain + digital signature service detecting update/delete/reordering/tail truncation. */
public final class CpfTamperAuditService implements CpfTamperAuditOperations {
    private static final String GENESIS = "GENESIS";
    private final CpfTamperAuditStore store;
    private final CpfDigitalSignatureOperations signatures;
    private final String keyId;
    private final String algorithm;
    private final Clock clock;

    public CpfTamperAuditService(CpfTamperAuditStore store, CpfDigitalSignatureOperations signatures, String keyId, String algorithm) {
        this(store, signatures, keyId, algorithm, Clock.systemUTC());
    }

    CpfTamperAuditService(CpfTamperAuditStore store, CpfDigitalSignatureOperations signatures, String keyId, String algorithm, Clock clock) {
        this.store = Objects.requireNonNull(store, "store");
        this.signatures = Objects.requireNonNull(signatures, "signatures");
        this.keyId = required(keyId);
        this.algorithm = required(algorithm);
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CpfTamperAuditRecord append(String transactionId, String actor, String action, byte[] canonicalMaskedPayload) {
        for (int attempt = 0; attempt < 8; attempt++) {
            var head = store.head();
            var latest = store.latest();
            if (head.sequence() == 0 && latest.isPresent()) throw new IllegalStateException("AUDIT_HEAD_RECORD_MISMATCH");
            if (head.sequence() > 0 && (latest.isEmpty() || latest.get().sequence() != head.sequence() || !latest.get().currentHash().equals(head.currentHash()))) {
                throw new IllegalStateException("AUDIT_HEAD_RECORD_MISMATCH");
            }
            String previous = head.currentHash();
            long sequence = head.sequence() + 1;
            Instant occurredAt = Instant.now(clock);
            String payloadHash = hex(sha(canonicalMaskedPayload == null ? new byte[0] : canonicalMaskedPayload));
            byte[] canonical = canonical(sequence, transactionId, actor, action, payloadHash, previous, occurredAt);
            String currentHash = hex(sha(canonical));
            var signature = signatures.sign(transactionId, keyId, algorithm, canonical);
            var record = new CpfTamperAuditRecord(sequence, transactionId, actor, action, payloadHash, previous, currentHash, signature, occurredAt);
            if (store.append(previous, record)) return record;
        }
        throw new IllegalStateException("Audit append contention exceeded retry bound");
    }

    @Override
    public Verification verify(long fromSequence, int limit) {
        if (fromSequence < 1) throw new IllegalArgumentException("fromSequence must be >= 1");
        if (limit < 1 || limit > 10000) throw new IllegalArgumentException("limit 1..10000");
        var head = store.head();
        if (head.sequence() == 0) return new Verification(true, 0, null);

        long scanFrom = Math.max(1, fromSequence - 1);
        int scanLimit = Math.min(10000, limit + (scanFrom < fromSequence ? 1 : 0));
        var rows = store.scan(scanFrom, scanLimit);
        if (rows.isEmpty()) return new Verification(false, 0, "MISSING_RECORDS_BEFORE_HEAD");

        long checked = 0;
        CpfTamperAuditRecord previous = null;
        for (CpfTamperAuditRecord record : rows) {
            if (previous == null) {
                if (record.sequence() == 1 && !GENESIS.equals(record.previousHash())) return new Verification(false, checked, "INVALID_GENESIS");
                if (record.sequence() > 1 && record.sequence() != scanFrom) return new Verification(false, checked, "SEQUENCE_GAP");
            } else {
                if (record.sequence() != previous.sequence() + 1) return new Verification(false, checked, "SEQUENCE_GAP");
                if (!previous.currentHash().equals(record.previousHash())) return new Verification(false, checked, "CHAIN_ORDER_MISMATCH");
            }
            byte[] canonical = canonical(record.sequence(), record.transactionId(), record.actor(), record.action(), record.payloadHash(), record.previousHash(), record.occurredAt());
            if (!hex(sha(canonical)).equals(record.currentHash())) return new Verification(false, checked, "HASH_MISMATCH");
            if (!signatures.verify(record.transactionId(), canonical, record.signature())) return new Verification(false, checked, "SIGNATURE_INVALID");
            previous = record;
            if (record.sequence() >= fromSequence) checked++;
        }

        CpfTamperAuditRecord last = rows.get(rows.size() - 1);
        long expectedLast = Math.min(head.sequence(), fromSequence + limit - 1L);
        if (last.sequence() < expectedLast) return new Verification(false, checked, "MISSING_RECORDS_BEFORE_HEAD");
        if (last.sequence() == head.sequence() && !last.currentHash().equals(head.currentHash())) return new Verification(false, checked, "HEAD_HASH_MISMATCH");
        return new Verification(true, checked, null);
    }

    private static byte[] canonical(long sequence, String transactionId, String actor, String action, String payloadHash, String previousHash, Instant occurredAt) {
        String value = sequence + "|" + required(transactionId) + "|" + Objects.requireNonNullElse(actor, "") + "|" + required(action) + "|" + required(payloadHash) + "|" + required(previousHash) + "|" + occurredAt;
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] sha(byte[] value) {
        try { return MessageDigest.getInstance("SHA-256").digest(value); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }

    private static String hex(byte[] value) { return HexFormat.of().formatHex(value); }
    private static String required(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("required value missing");
        return value;
    }
}
