package com.cpf.admin.opr.filejob;

/** File Job Consumer가 Side Effect 결과의 확정 가능성을 명시하는 예외입니다. */
public final class AdmFileJobDispatchException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public enum Certainty { NOT_APPLIED, UNKNOWN }
    private final Certainty certainty;

    private AdmFileJobDispatchException(Certainty certainty, String message, Throwable cause) {
        super(message, cause);
        this.certainty = java.util.Objects.requireNonNull(certainty, "certainty");
    }
    public Certainty certainty() { return certainty; }
    public static AdmFileJobDispatchException notApplied(String message) {
        return new AdmFileJobDispatchException(Certainty.NOT_APPLIED, message, null);
    }
    public static AdmFileJobDispatchException notApplied(String message, Throwable cause) {
        return new AdmFileJobDispatchException(Certainty.NOT_APPLIED, message, cause);
    }
    public static AdmFileJobDispatchException unknown(String message, Throwable cause) {
        return new AdmFileJobDispatchException(Certainty.UNKNOWN, message, cause);
    }
}
