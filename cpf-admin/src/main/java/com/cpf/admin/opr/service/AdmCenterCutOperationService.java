package com.cpf.admin.opr.service;
import com.cpf.core.api.batch.CpfCenterCutOperationsPort;
import org.springframework.stereotype.Service;
import java.util.*;
/** ADM은 Center-Cut Owner DB에 접근하지 않고 BAT 운영 Port만 소비합니다. */
@Service
public class AdmCenterCutOperationService extends com.cpf.admin.common.base.AdmBaseService {
 private final CpfCenterCutOperationsPort port;public AdmCenterCutOperationService(CpfCenterCutOperationsPort port){this.port=port;}
 public List<Map<String,Object>> findJobs(){return port.findJobs();}public Map<String,Object> findJobDetail(String id){return port.findJobDetail(id);}public List<Map<String,Object>> findParameters(String id){return port.findParameters(id);}public Map<String,Object> findSummary(String id){return port.findSummary(id);}public List<Map<String,Object>> findTargets(String id,String status,int limit){return port.findTargets(id,status,limit);}public List<Map<String,Object>> findResults(String id,String status,int limit){return port.findResults(id,status,limit);}public Map<String,Object> findResultDetail(String id){return port.findResultDetail(id);}
}
