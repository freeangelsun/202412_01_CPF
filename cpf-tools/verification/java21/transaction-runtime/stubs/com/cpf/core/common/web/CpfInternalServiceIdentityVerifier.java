package com.cpf.core.common.web; import jakarta.servlet.http.HttpServletRequest; public interface CpfInternalServiceIdentityVerifier {boolean isTrusted(HttpServletRequest r,String s,String i);}
