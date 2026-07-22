package com.cpf.core.common.logging.segment;

import com.cpf.core.common.logging.TransactionHeader;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 현재 스레드에서 실행 중인 거래 구간을 보관하고 하위 호출 헤더로 전파할 값을 제공합니다.
 */
public final class TransactionSegmentContext {
    private static final ThreadLocal<Deque<TransactionSegmentFrame>> FRAMES =
            ThreadLocal.withInitial(ArrayDeque::new);

    private TransactionSegmentContext() {
    }

    public static TransactionSegmentFrame currentFrame() {
        return FRAMES.get().peek();
    }

    public static String currentSegmentId() {
        TransactionSegmentFrame frame = currentFrame();
        return frame != null ? frame.transactionSegmentId() : null;
    }

    public static String rootTransactionGlobalId() {
        TransactionSegmentFrame frame = currentFrame();
        return frame != null ? frame.rootTransactionGlobalId() : null;
    }

    public static int currentCallDepth() {
        TransactionSegmentFrame frame = currentFrame();
        return frame != null ? frame.callDepth() : -1;
    }

    public static void push(TransactionSegmentFrame frame) {
        if (frame != null) {
            FRAMES.get().push(frame);
        }
    }

    public static void pop(String transactionSegmentId) {
        Deque<TransactionSegmentFrame> frames = FRAMES.get();
        if (frames.isEmpty()) {
            return;
        }
        TransactionSegmentFrame current = frames.peek();
        if (current != null && current.transactionSegmentId().equals(transactionSegmentId)) {
            frames.pop();
        } else {
            frames.removeIf(frame -> frame.transactionSegmentId().equals(transactionSegmentId));
        }
        if (frames.isEmpty()) {
            FRAMES.remove();
        }
    }

    public static void clear() {
        FRAMES.remove();
    }

    public static String incomingParentSegmentId(TransactionHeader header) {
        if (header == null) {
            return null;
        }
        return firstText(header.getTransactionSegmentId(), header.getParentSegmentId());
    }

    public static int incomingCallDepth(TransactionHeader header) {
        if (header == null || header.getCallDepth() == null || header.getCallDepth().isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(header.getCallDepth().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static String firstText(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    public record TransactionSegmentFrame(
            String transactionSegmentId,
            String rootTransactionGlobalId,
            int callDepth) {
    }
}
