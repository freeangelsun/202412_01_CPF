package com.cpf.core.api.config;
import java.util.*;
/** 실행 중인 CPF 설정의 변경 가능 여부와 Secret 분리 정책을 조회합니다. */
public interface CpfConfigCatalog { List<CpfConfigDescriptor> descriptors(); Optional<CpfConfigDescriptor> find(String prefix); }
