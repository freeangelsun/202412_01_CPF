package com.cpf.education.integration.graphql;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/** Resolver가 업무 로직을 복제하지 않도록 분리한 Education Application Service. */
@Service
public class EducationPortfolioApplicationService {
    private final ConcurrentHashMap<String,String> names=new ConcurrentHashMap<>();
    public EducationPortfolioApplicationService(){ names.put("P-100","Education Portfolio"); }
    public EducationPortfolio find(String id){String name=names.get(id);return name==null?null:new EducationPortfolio(id,name,List.of("A-1","A-2"),List.of("Checking","Savings"));}
    public EducationPortfolio rename(String id,String name){if(id==null||id.isBlank()||name==null||name.isBlank())throw new IllegalArgumentException("id/name required");names.put(id,name.trim());return find(id);}
}
