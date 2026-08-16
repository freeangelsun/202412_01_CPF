package com.cpf.common.message.service;

import com.cpf.common.message.api.CpfMessageRecord;
import com.cpf.common.message.api.CpfResponseCodeRecord;
import java.util.Locale;

/** Resolver가 사용하는 internal cache/store boundary입니다. */
interface CmnErrorCatalogStore {
    CpfResponseCodeRecord response(String code);
    CpfMessageRecord message(String code, Locale locale);
}
