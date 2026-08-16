package com.cpf.common.template;
import java.util.Optional;
/** 고객 저장소/Remote Config가 구현하는 Template provider SPI입니다. */
public interface CmnTemplateProvider { Optional<CmnTemplateDefinition> findActive(String templateCode,String channel); }
