package cpf.xyz.edu.logging;

import cpf.pfw.common.logging.file.CpfFileLogWriter;
import cpf.pfw.common.logging.TransactionLogRecord;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 업무 모듈에서 PFW 구조화 파일 로그를 사용하는 교육 샘플입니다.
 *
 * <p>업무 코드는 파일 경로를 직접 조합하거나 파일에 직접 쓰지 않고, PFW가 제공하는
 * {@link CpfFileLogWriter}를 호출합니다. 이 경계를 지키면 모듈·인스턴스·일자별 파일명,
 * JSON Lines 형식, 민감정보 마스킹과 보관 정책을 프레임워크가 일관되게 적용할 수 있습니다.</p>
 */
public class XyzFileLogEducationSample {
    private final CpfFileLogWriter fileLogWriter;

    public XyzFileLogEducationSample(CpfFileLogWriter fileLogWriter) {
        this.fileLogWriter = fileLogWriter;
    }

    /**
     * 애플리케이션 기동·설정·상태 변경처럼 거래에 속하지 않는 이벤트를 기록합니다.
     */
    public void writeApplicationLog(String message) {
        Map<String, Object> event = baseEvent("application", "APPLICATION_STATE");
        event.put("status", "SUCCESS");
        event.put("message", message);
        fileLogWriter.writeEvent("XYZ", "application", event);
    }

    /**
     * 온라인 업무 처리 결과를 전역 거래 ID와 세그먼트 ID로 연결해 기록합니다.
     */
    public void writeTransactionLog(
            String transactionId,
            String transactionGlobalId,
            String transactionSegmentId) {
        TransactionLogRecord record = TransactionLogRecord.builder()
                .transactionId(transactionGlobalId)
                .businessTransactionId(transactionId)
                .spanId(transactionSegmentId)
                .moduleId("XYZ")
                .logType("SUCCESS")
                .httpMethod("POST")
                .uri("/api/v1/xyz/edu/logging")
                .httpStatus(200)
                .responseCode("00000000")
                .startTime(fileLogWriter.currentLogDate().atStartOfDay())
                .durationMs(12L)
                .build();
        fileLogWriter.writeTransaction(record, Map.of("education.sample", "XYZ_FILE_LOG"), null);
    }

    /**
     * 실패 이벤트를 error 로그로 분리합니다. 실제 예외 객체 전체보다 운영 식별 코드와
     * 마스킹 가능한 메시지를 구조화 필드로 전달하는 방식을 보여줍니다.
     */
    public void writeErrorLog(String failureCode, String failureMessage) {
        Map<String, Object> event = baseEvent("error", "EDU_FAILURE");
        event.put("status", "FAILURE");
        event.put("failureCode", failureCode);
        event.put("failureMessage", failureMessage);
        fileLogWriter.writeEvent("XYZ", "error", event);
    }

    /**
     * 개발자가 실수로 비밀번호나 인증 헤더를 이벤트에 넣어도 PFW writer가 원문 저장을
     * 차단하는 예제입니다. 이 보호 기능이 민감정보를 로그에 넣어도 된다는 뜻은 아니며,
     * 업무 코드는 가능한 한 민감정보 필드 자체를 전달하지 않아야 합니다.
     */
    public void writeMaskingGuardExample(String password, String authorization) {
        Map<String, Object> event = baseEvent("error", "MASKING_GUARD");
        event.put("status", "BLOCKED");
        event.put("password", password);
        event.put("authorization", authorization);
        fileLogWriter.writeEvent("XYZ", "error", event);
    }

    private Map<String, Object> baseEvent(String logType, String eventType) {
        Map<String, Object> event = new LinkedHashMap<>(fileLogWriter.newBaseEvent("XYZ", logType));
        event.put("eventType", eventType);
        return event;
    }
}
