package com.cpf.reference.edu.runtime;
import com.cpf.reference.edu.runtime.application.*;
import java.util.*;
final class EduFullReferenceTestRegistry {
 private EduFullReferenceTestRegistry(){}
 static EduCapabilityRegistry create(){
  List<EduCapabilityContributor> contributors=new ArrayList<>();
  contributors.add(new CoreEduCapabilityContributor());
  for(String name:List.of(
    "com.cpf.reference.batch.config.ReferenceBatchCapabilityContributor",
    "com.cpf.reference.optional.backoffice.config.ReferenceBackofficeCapabilityContributor",
    "com.cpf.reference.optional.operations.config.ReferenceOperationsCapabilityContributor",
    "com.cpf.reference.optional.gateway.config.ReferenceGatewayCapabilityContributor")){
   try{contributors.add((EduCapabilityContributor)Class.forName(name).getConstructor().newInstance());}
   catch(ClassNotFoundException ignored){}
   catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot load EDU contributor "+name,e);}
  }
  return new EduCapabilityRegistry(contributors);
 }
}
