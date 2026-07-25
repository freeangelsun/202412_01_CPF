package com.cpf.batch.operation;

import com.cpf.core.common.execution.CpfSharedApi;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/bat/internal/center-cut")
@CpfSharedApi(id="SBATCT0001", name="BATCenterCutOperations", allowedCallers={"ADM"})
public class BatCenterCutInternalController {
    private final BatCenterCutOperationsFacade service;
    public BatCenterCutInternalController(BatCenterCutOperationsFacade service){this.service=service;}
    @PostMapping("/{operation}") public Object invoke(@PathVariable String operation,@RequestBody(required=false) Map<String,Object> p){Map<String,Object> v=p==null?Map.of():p;return switch(operation){
        case "findJobs"->service.findJobs();case "findJobDetail"->service.findJobDetail(s(v,"centerCutJobId"));case "findParameters"->service.findParameters(s(v,"centerCutJobId"));case "findSummary"->service.findSummary(s(v,"centerCutJobId"));
        case "findTargets"->service.findTargets(s(v,"centerCutJobId"),s(v,"statusCode"),i(v,"limit",100));case "findResults"->service.findResults(s(v,"centerCutJobId"),s(v,"resultStatus"),i(v,"limit",100));case "findResultDetail"->service.findResultDetail(s(v,"resultId"));
        default->throw new IllegalArgumentException("unsupported center-cut operation: "+operation);};}
    private static String s(Map<String,Object> p,String k){Object v=p.get(k);return v==null?null:String.valueOf(v);}private static int i(Map<String,Object> p,String k,int d){Object v=p.get(k);if(v==null)return d;try{return Integer.parseInt(String.valueOf(v));}catch(Exception e){return d;}}
}
