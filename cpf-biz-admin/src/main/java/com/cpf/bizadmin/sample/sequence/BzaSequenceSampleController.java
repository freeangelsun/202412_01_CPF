package com.cpf.bizadmin.sample.sequence;

import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/bza/sample/sequence")
@ConditionalOnProperty(prefix="cpf.bza.sample.sequence",name="enabled",havingValue="true")
@Tag(name="BZA-Sequence-Sample",description="선택형 업무 채번 Customization Sample")
public class BzaSequenceSampleController {
    private final BzaSequenceSampleService service;public BzaSequenceSampleController(BzaSequenceSampleService service){this.service=service;}
    @GetMapping("/rules") @CpfOnlineTransaction(id="OBZASQ0001",name="BzaSequenceSampleRules") public ResponseEntity<?> rules(){return ResponseEntity.ok(service.rules());}
    @PostMapping("/rules") @CpfOnlineTransaction(id="OBZASQ0002",name="BzaSequenceSampleRuleSave") public ResponseEntity<?> save(@RequestBody BzaSequenceSampleService.RuleRequest r,@RequestAttribute("bza.operatorId")String user){return ResponseEntity.ok(service.save(r,user));}
    @PostMapping("/rules/{code}/issue") @CpfOnlineTransaction(id="OBZASQ0003",name="BzaSequenceSampleIssue") public ResponseEntity<?> issue(@PathVariable String code,@RequestBody Map<String,Object> body,@RequestAttribute("bza.operatorId")String user){return ResponseEntity.ok(service.issue(code,user,String.valueOf(body.getOrDefault("reason",""))));}
    @GetMapping("/history") @CpfOnlineTransaction(id="OBZASQ0004",name="BzaSequenceSampleHistory") public ResponseEntity<?> history(@RequestParam(required=false)String ruleCode,@RequestParam(defaultValue="100")int limit){return ResponseEntity.ok(service.history(ruleCode,limit));}
}
