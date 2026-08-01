package com.cpf.reference.edu.runtime.api;
import com.cpf.reference.edu.runtime.application.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestControllerAdvice(assignableTypes=EduCapabilityController.class)
public class EduApiExceptionHandler {
    @ExceptionHandler(EduValidationException.class) ResponseEntity<Map<String,Object>> validation(Exception e){return error(HttpStatus.BAD_REQUEST,"CPF-EDU-400",e);}
    @ExceptionHandler(EduAuthorizationException.class) ResponseEntity<Map<String,Object>> auth(Exception e){return error(HttpStatus.FORBIDDEN,"CPF-EDU-403",e);}
    @ExceptionHandler(EduConflictException.class) ResponseEntity<Map<String,Object>> conflict(Exception e){return error(HttpStatus.CONFLICT,"CPF-EDU-409",e);}
    @ExceptionHandler(NoSuchElementException.class) ResponseEntity<Map<String,Object>> missing(Exception e){return error(HttpStatus.NOT_FOUND,"CPF-EDU-404",e);}
    private ResponseEntity<Map<String,Object>> error(HttpStatus s,String code,Exception e){return ResponseEntity.status(s).body(Map.of("code",code,"message",e.getMessage(),"status",s.value()));}
}
