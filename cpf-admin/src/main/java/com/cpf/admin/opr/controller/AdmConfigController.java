package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.common.management.*;
import com.cpf.common.parameter.dto.CommonConfigRequest;
import com.cpf.core.api.context.CpfContexts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** ADM Runtime 설정을 canonical Common Management API로 조회·변경합니다. */
@RestController
@RequestMapping("/adm/api/configs")
@Tag(name="ADM-CPF Configs",description="CPF 공통 설정 관리 API")
public class AdmConfigController extends com.cpf.admin.common.base.AdmBaseController {
    private final CpfCommonManagementApi common; private final AdmAuditLogService audit;
    public AdmConfigController(CpfCommonManagementApi common,AdmAuditLogService audit){this.common=common;this.audit=audit;}

    @GetMapping @Operation(operationId="admConfigFindConfigs",summary="공통 설정 목록 조회")
    public ResponseEntity<List<Map<String,Object>>> findConfigs(HttpServletRequest r){requireOperator(r);return ResponseEntity.ok(common.search(CpfCommonResource.PARAMETER,null,0,200,true,null).content().stream().map(this::maskSecret).toList());}
    @GetMapping("/{configId}") @Operation(operationId="admConfigFindConfig",summary="공통 설정 상세 조회")
    public ResponseEntity<Map<String,Object>> findConfig(@PathVariable Long configId,HttpServletRequest r){requireOperator(r);return ResponseEntity.ok(maskSecret(common.get(CpfCommonResource.PARAMETER,Map.of("config_id",configId))));}
    @PostMapping @Operation(operationId="admConfigCreateConfig",summary="공통 설정 등록")
    public ResponseEntity<Map<String,Object>> createConfig(@Valid @RequestBody CommonConfigRequest b,HttpServletRequest r){String actor=requireOperator(r),reason=audit.requireReason(b.getReason());Map<String,Object> out=common.create(CpfCommonResource.PARAMETER,new CpfCommonMutation(Map.of(),values(b),null,reason),actor);record(r,actor,"CONFIG_CREATE",String.valueOf(value(out,"config_id",b.getConfigKey())),reason,null,maskSecret(out));return ResponseEntity.ok(maskSecret(out));}
    @PutMapping("/{configId}") @Operation(operationId="admConfigUpdateConfig",summary="공통 설정 수정")
    public ResponseEntity<Map<String,Object>> updateConfig(@PathVariable Long configId,@Valid @RequestBody CommonConfigRequest b,HttpServletRequest r){String actor=requireOperator(r),reason=audit.requireReason(b.getReason());Map<String,Object> before=maskSecret(common.get(CpfCommonResource.PARAMETER,Map.of("config_id",configId)));Map<String,Object> out=common.update(CpfCommonResource.PARAMETER,new CpfCommonMutation(Map.of("config_id",configId),values(b),null,reason),actor);record(r,actor,"CONFIG_UPDATE",String.valueOf(configId),reason,before,maskSecret(out));return ResponseEntity.ok(maskSecret(out));}
    @DeleteMapping("/{configId}") @Operation(operationId="admConfigDeleteConfig",summary="공통 설정 비활성")
    public ResponseEntity<List<Map<String,Object>>> deleteConfig(@PathVariable Long configId,@RequestParam String reason,HttpServletRequest r){String actor=requireOperator(r),required=audit.requireReason(reason);Map<String,Object> before=maskSecret(common.get(CpfCommonResource.PARAMETER,Map.of("config_id",configId)));common.delete(CpfCommonResource.PARAMETER,new CpfCommonMutation(Map.of("config_id",configId),Map.of(),null,required),actor);record(r,actor,"CONFIG_DISABLE",String.valueOf(configId),required,before,null);return ResponseEntity.ok(common.search(CpfCommonResource.PARAMETER,null,0,200,true,null).content().stream().map(this::maskSecret).toList());}

    private Map<String,Object> values(CommonConfigRequest r){Map<String,Object> v=new LinkedHashMap<>();v.put("config_key",r.getConfigKey());v.put("config_value",r.getConfigValue());v.put("config_type",r.getConfigType());if(r.getDescription()!=null)v.put("description",r.getDescription());v.put("encrypted_yn",r.getEncryptedYn());v.put("use_yn",r.getUseYn());return v;}
    private Map<String,Object> maskSecret(Map<String,Object> source){Map<String,Object> out=new LinkedHashMap<>(source);Object encrypted=value(out,"encrypted_yn","N");if("Y".equalsIgnoreCase(String.valueOf(encrypted)))for(String k:List.copyOf(out.keySet()))if(k.equalsIgnoreCase("config_value"))out.put(k,"[MASKED]");return out;}
    private Object value(Map<String,Object> map,String key,Object fallback){for(var e:map.entrySet())if(e.getKey().equalsIgnoreCase(key))return e.getValue();return fallback;}
    private void record(HttpServletRequest req,String actor,String action,String key,String reason,Object before,Object after){audit.record(CpfContexts.transactionId(),actor,action,"CMN_PARAMETER",key,reason,before==null?null:String.valueOf(before),after==null?null:String.valueOf(after),action,req.getRemoteAddr());}
}
