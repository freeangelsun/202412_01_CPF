package com.cpf.batch.control.job;
import com.cpf.batch.api.BatchJobDefinition;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import java.util.*;
/** BAT Owner의 Versioned Job Definition API입니다. */
@RestController @RequestMapping("/api/v1/batch/job-definitions")
public class BatchJobDefinitionController {
 private final BatchJobDefinitionService service;public BatchJobDefinitionController(BatchJobDefinitionService service){this.service=service;}
 @GetMapping public List<Map<String,Object>> list(@RequestParam(required=false)String jobId,@RequestParam(required=false)String state,@RequestParam(defaultValue="200")int limit){return service.list(jobId,state,limit);}
 @PostMapping("/validate") public BatchJobDefinitionService.ValidationResult validate(@RequestBody BatchJobDefinition definition){return service.validate(definition);}
 @PostMapping("/drafts") public ResponseEntity<BatchJobDefinitionService.SavedDefinition> save(@RequestBody BatchJobDefinition definition){return ResponseEntity.status(201).body(service.saveDraft(definition));}
 @PostMapping("/{jobId}/versions/{version}/transition") public BatchJobDefinitionService.SavedDefinition transition(@PathVariable String jobId,@PathVariable long version,@RequestBody TransitionRequest request){return service.transition(jobId,version,request.expectedRowVersion(),request.targetState(),request.operatorId(),request.reason());}
 public record TransitionRequest(long expectedRowVersion,String targetState,String operatorId,String reason){}
}
