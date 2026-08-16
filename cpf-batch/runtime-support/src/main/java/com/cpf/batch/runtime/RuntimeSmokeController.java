package com.cpf.batch.runtime;
import com.cpf.batch.api.RuntimeRegistration;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/internal/v1/runtime")
public class RuntimeSmokeController {
 private final RuntimeRegistration registration;private final RuntimeStateProvider state;
 public RuntimeSmokeController(RuntimeRegistration registration,RuntimeStateProvider state){this.registration=registration;this.state=state;}
 @GetMapping("/smoke") public Map<String,Object> smoke(){return Map.of("serviceId",registration.serviceId(),"instanceId",registration.instanceId(),"role",registration.runtimeRole().name(),"version",registration.artifactVersion(),"ready",state.ready(),"fencingToken",state.fencingToken());}
}
