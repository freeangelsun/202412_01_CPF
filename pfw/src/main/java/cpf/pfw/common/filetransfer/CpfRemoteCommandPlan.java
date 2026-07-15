package cpf.pfw.common.filetransfer;

import java.util.List;

/**
 * 원격 명령의 credential 원문 없이 생성되는 실행 계획입니다.
 */
public record CpfRemoteCommandPlan(
        boolean accepted,
        boolean executed,
        int exitCode,
        List<String> command,
        String output,
        String detail) {
}
