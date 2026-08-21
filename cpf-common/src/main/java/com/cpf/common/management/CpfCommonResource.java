package com.cpf.common.management;

import java.util.List;
import java.util.Set;

/** MBW Backoffice가 Common Public Management API를 통해 관리할 수 있는 CPF Product Service 자원 allowlist입니다. */
public enum CpfCommonResource {
    CODE("CMN_CODE", List.of("code_id"), "code_id", "use_yn", null, null, null, "codeCache",
            Set.of("code_id","parent_id","code_key","code_value","description","use_yn"),
            List.of("code_key","code_value","description")),
    PARAMETER("CMN_PARAMETER", List.of("config_id"), "config_id", "use_yn", null, null, null, "configCache",
            Set.of("config_id","config_key","config_value","config_type","description","encrypted_yn","use_yn"),
            List.of("config_key","config_type","description")),
    MESSAGE("CMN_MESSAGE", List.of("message_id"), "message_id", "use_yn", "catalog_version", "effective_from", "effective_to", "messageCache",
            Set.of("message_id","message_code","locale","message_format_type","external_message","internal_message","parameter_count","parameter_sample","parameter_schema_json","escape_html_yn","mask_arguments_yn","effective_from","effective_to","catalog_version","description","use_yn"),
            List.of("message_code","locale","description")),
    RESPONSE_CODE("CMN_RESPONSE_CODE", List.of("response_code"), "response_code", "use_yn", "catalog_version", "effective_from", "effective_to", "responseCodeCache",
            Set.of("response_code","message_code","result_type","module_id","response_group","sequence_no","category","retry_disposition","exposure","effective_from","effective_to","catalog_version","description","use_yn"),
            List.of("response_code","message_code","module_id","response_group","description")),
    CALENDAR("CMN_BUSINESS_CALENDAR_DAY", List.of("calendar_id","business_date"), "business_date", null, "version_no", null, null, "businessCalendar",
            Set.of("calendar_id","business_date","business_day_yn","day_type","institution_code","reason","version_no"),
            List.of("calendar_id","day_type","institution_code","reason")),
    TEMPLATE("CMN_TEMPLATE_DEFINITION", List.of("template_code","template_version","channel_code"), "template_code", "active_yn", "revision_no", null, null, "commonTemplate",
            Set.of("template_code","template_version","channel_code","template_body","allowed_variables","status_code","active_yn","revision_no","approved_by","approved_at"),
            List.of("template_code","channel_code","status_code"));

    private final String table;
    private final List<String> keyColumns;
    private final String orderColumn;
    private final String activeColumn;
    private final String versionColumn;
    private final String effectiveFrom;
    private final String effectiveTo;
    private final String cacheName;
    private final Set<String> writableColumns;
    private final List<String> searchableColumns;

    CpfCommonResource(String table, List<String> keyColumns, String orderColumn, String activeColumn, String versionColumn,
                      String effectiveFrom, String effectiveTo, String cacheName, Set<String> writableColumns, List<String> searchableColumns) {
        this.table=table; this.keyColumns=keyColumns; this.orderColumn=orderColumn; this.activeColumn=activeColumn; this.versionColumn=versionColumn;
        this.effectiveFrom=effectiveFrom; this.effectiveTo=effectiveTo; this.cacheName=cacheName; this.writableColumns=writableColumns; this.searchableColumns=searchableColumns;
    }
    public String table(){return table;} public List<String> keyColumns(){return keyColumns;} public String orderColumn(){return orderColumn;}
    public String activeColumn(){return activeColumn;} public String versionColumn(){return versionColumn;} public String effectiveFrom(){return effectiveFrom;}
    public String effectiveTo(){return effectiveTo;} public String cacheName(){return cacheName;} public Set<String> writableColumns(){return writableColumns;}
    public List<String> searchableColumns(){return searchableColumns;}
}
