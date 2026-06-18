package cpf.pfw.common.exception;

/**
 * 議고쉶 ??곸씠 ?놁쓣 ???ъ슜?섎뒗 ?쒖? ?덉쇅?낅땲??
 */
public class CpfNotFoundException extends CpfException {
    public CpfNotFoundException(String detail) {
        super(CpfErrorCode.NOT_FOUND, detail);
    }
}

