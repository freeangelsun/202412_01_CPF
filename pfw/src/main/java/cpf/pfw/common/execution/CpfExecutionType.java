package cpf.pfw.common.execution;

/** CPF 표준 실행 유형입니다. */
public enum CpfExecutionType {
    ONLINE('O'),
    BATCH('B');

    private final char prefix;

    CpfExecutionType(char prefix) {
        this.prefix = prefix;
    }

    public char prefix() {
        return prefix;
    }

    public static CpfExecutionType fromPrefix(char prefix) {
        return switch (prefix) {
            case 'O' -> ONLINE;
            case 'B' -> BATCH;
            default -> throw new IllegalArgumentException("표준 실행 ID 유형은 O 또는 B여야 합니다.");
        };
    }
}
