package com.cpf.starter.integration.soap;
import java.util.Objects;
/** SOAP 호출 결과. 민감한 raw envelope를 오류에 보관하지 않습니다. */
public record CpfSoapResult<T>(CpfSoapStatus status, T body, String errorCode) {
    public CpfSoapResult { status=Objects.requireNonNull(status,"status"); if(status==CpfSoapStatus.SUCCESS&&body==null)throw new IllegalArgumentException("SUCCESS body required"); }
    public static <T> CpfSoapResult<T> success(T body){return new CpfSoapResult<>(CpfSoapStatus.SUCCESS,body,null);}
    public static <T> CpfSoapResult<T> failed(String code){return new CpfSoapResult<>(CpfSoapStatus.FAILED,null,code);}
    public static <T> CpfSoapResult<T> unknown(String code){return new CpfSoapResult<>(CpfSoapStatus.UNKNOWN,null,code);}
}
