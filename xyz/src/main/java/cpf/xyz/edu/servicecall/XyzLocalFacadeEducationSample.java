package cpf.xyz.edu.facade;

/**
 * Controller 직접 호출 대신 facade 경계를 사용하는 샘플입니다.
 */
public class XyzLocalFacadeEducationSample {

    public FacadeResult call(String facadeName, String command) {
        return new FacadeResult(facadeName, command, "LOCAL_FACADE");
    }

    public record FacadeResult(String facadeName, String command, String callType) {
    }
}
