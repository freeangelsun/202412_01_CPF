package cpf.bat.edu.idempotency;

/**
 * 배치 idempotency key 생성 샘플입니다.
 */
public class BatIdempotencyEducationSample {

    public String key(String jobName, String businessDate, String parameterHash) {
        return jobName + ":" + businessDate + ":" + parameterHash;
    }
}
