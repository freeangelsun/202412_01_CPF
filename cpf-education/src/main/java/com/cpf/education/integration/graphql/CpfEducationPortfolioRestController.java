package com.cpf.education.integration.graphql;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST consumer proving GraphQL reuses the same application service rather than replacing REST. */
@RestController @RequestMapping("/api/education/portfolios")
/** CpfEducationPortfolioRestController 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfEducationPortfolioRestController {
 private final CpfEducationPortfolioService service;
 public CpfEducationPortfolioRestController(CpfEducationPortfolioService service){this.service=service;}
 @GetMapping("/{id}") @PreAuthorize("isAuthenticated()") public CpfEducationPortfolioService.Portfolio find(@PathVariable String id){return service.find(id);}
}
