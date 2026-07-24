package com.cpf.core.common.logging.segment;

import com.cpf.core.common.logging.TransactionHeader;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 현재 스레드에서 실행 중인 거래 구간을 보관합니다.
 *
 * <p>CPF 2.x 식별 정책은 거래 흐름 전체에 transactionId 하나를 승계하고,
 * 각 local/remote/async 구간만 transactionSegmentId/parentSegmentId로 분리합니다.</p>
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

    public static String currentTransactionId() {
        TransactionSegmentFrame frame = currentFrame();
        return frame != null ? frame.transactionId() : null;
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

    /**
     * 하위 구간은 호출자가 보낸 parentSegmentId를 부모로 사용합니다.
     * 구형 transport가 transactionSegmentId만 보낸 경우를 한시적으로 허용합니다.
     */
    public static String incomingParentSegmentId(TransactionHeader header) {
        if (header == null) {
            return null;
        }
        return firstText(header.getParentSegmentId(), header.getTransactionSegmentId());
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
            String transactionId,
            int callDepth) {

    }
}
