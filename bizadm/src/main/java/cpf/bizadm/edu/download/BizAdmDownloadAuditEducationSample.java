package cpf.bizadm.edu.download;

/**
 * 다운로드 권한과 감사 사유를 함께 확인하는 샘플입니다.
 */
public class BizAdmDownloadAuditEducationSample {

    public DownloadDecision decide(boolean allowed, String reason) {
        return new DownloadDecision(allowed && reason != null && !reason.isBlank(), allowed ? "AUDIT_REQUIRED" : "DENIED");
    }

    public record DownloadDecision(boolean downloadable, String status) {
    }
}
