import fs from "node:fs";
const [file="openapi/cpf-openapi.json"] = process.argv.slice(2);
const spec=JSON.parse(fs.readFileSync(file,"utf8"));
const required=[
 ["/adm/api/integration-closure/data-quality/quarantine/{id}/replay","post","admIntegrationDataQualityReplay"],
 ["/adm/api/approvals/requests","post","admApprovalRequest"],
 ["/adm/api/approvals/requests/{id}/decisions","post","admApprovalDecision"]
];
for(const [route,method,id] of required){const op=spec.paths?.[route]?.[method];if(!op||op.operationId!==id)throw new Error(`missing runtime operation ${method} ${route} ${id}`);}
console.log(`PASS runtime route assertions=${required.length}`);
