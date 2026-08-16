package com.cpf.integration.tcp;import javax.net.ssl.SSLContext;public interface CpfTcpTlsContextProvider {SSLContext current();String keyVersion();}
