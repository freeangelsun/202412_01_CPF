package com.cpf.admin.opr.parameter;

import com.cpf.core.api.parameter.CpfParameterReferenceCatalogPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 권한과 설치 Capability를 반영한 Parameter Reference 검색 API입니다. */
@RestController
@RequestMapping("/adm/api/parameter-references")
@Tag(name="ADM-ParameterReference",description="Secret/Path/Service/Endpoint/Group/File/Code Reference 검색 API")
public class AdmParameterReferenceController {
    private final CpfParameterReferenceCatalogPort catalog;
    public AdmParameterReferenceController(CpfParameterReferenceCatalogPort catalog){this.catalog=catalog;}

    @GetMapping
    @PreAuthorize("hasAuthority('REFERENCE_READ') or hasAuthority('BAT_JOB_READ') or hasAuthority('GATEWAY_READ')")
    @Operation(operationId="admParameterReferenceSearch",summary="Parameter Reference 검색")
    public CpfParameterReferenceCatalogPort.CatalogPage search(
            @RequestParam String referenceType,@RequestParam(required=false)String parentType,
            @RequestParam(required=false)String parentId,@RequestParam(required=false,name="q")String query,
            @RequestParam(defaultValue="0")int offset,@RequestParam(defaultValue="50")int limit,
            HttpServletRequest request) {
        Object actor=request.getAttribute("adm.operatorId");
        return catalog.search(new CpfParameterReferenceCatalogPort.ReferenceQuery(referenceType,parentType,parentId,query,offset,limit,actor instanceof String v?v:null));
    }
}
