package cpf.pfw.common.servicecall;

/**
 * 서비스 호출 이력을 pfwDB에 기록합니다.
 */
public class CpfServiceCallLogWriter {
    private final CpfServiceRegistryRepository repository;

    public CpfServiceCallLogWriter(CpfServiceRegistryRepository repository) {
        this.repository = repository;
    }

    public void write(
            ServiceCallRequest request,
            ServiceCallResolvedTarget target,
            String callStatus,
            Integer httpStatus,
            long durationMillis,
            String failureCode,
            String failureMessage) {
        repository.insertCallHistory(request, target, callStatus, httpStatus, durationMillis, failureCode, failureMessage);
    }
}
