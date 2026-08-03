package com.cpf.starter.runtimecontrol.spi; import java.util.Map; public final class CpfRuntimePayloadReader {public static Object value(Map<String,Object> p,String k){return p==null?null:p.get(k);}}
