package cpf.cmn.edu.masking;

/**
 * 프로젝트 공통 화면/파일 마스킹 helper 샘플입니다.
 */
public class CmnMaskingEducationSample {

    public String phone(String phoneNo) {
        if (phoneNo == null || phoneNo.length() < 7) {
            return "";
        }
        return phoneNo.substring(0, 3) + "****" + phoneNo.substring(phoneNo.length() - 4);
    }
}
