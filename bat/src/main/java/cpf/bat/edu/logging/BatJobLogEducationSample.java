package cpf.bat.edu.logging;

import java.nio.file.Path;

/**
 * jobExecutionId 기준 로그 경로를 계산하는 샘플입니다.
 */
public class BatJobLogEducationSample {

    public Path logPath(Path basePath, String jobName, long jobExecutionId) {
        return basePath.resolve(jobName).resolve(String.valueOf(jobExecutionId)).resolve("job.log");
    }
}
