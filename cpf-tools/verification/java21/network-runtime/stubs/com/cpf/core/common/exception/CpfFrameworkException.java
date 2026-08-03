package com.cpf.core.common.exception;
import java.util.Map;
public class CpfFrameworkException extends RuntimeException {
  public final CpfFrameworkErrorCode code; public final Map<String,?> details;
  public CpfFrameworkException(CpfFrameworkErrorCode code,String message,Map<String,?> details){super(message);this.code=code;this.details=details;}
}
