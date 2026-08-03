package com.cpf.core.api.error; public record CpfResolvedResponse(int httpStatus,String errorCode,String messageCode,String responseCode,String externalMessage){}
