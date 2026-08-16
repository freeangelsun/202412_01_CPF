#!/usr/bin/env python3
"""Generate the DB-provider OpenAPI artifact from the canonical operability contract."""
from __future__ import annotations
import argparse, json
from pathlib import Path

def main() -> int:
    p=argparse.ArgumentParser()
    p.add_argument("--contract", required=True)
    p.add_argument("--output", required=True)
    a=p.parse_args()
    contract=json.loads(Path(a.contract).read_text(encoding="utf-8-sig"))
    caps=[item["id"] for item in contract["capabilities"]]
    vendors=contract["officialVendors"]
    states=contract["publicContract"]["resultStates"]
    required=contract["publicContract"]["requestRequiredFields"]
    operation={
      "type":"object",
      "required":required,
      "additionalProperties":False,
      "properties":{
        "operationId":{"type":"string","pattern":"^[A-Za-z0-9][A-Za-z0-9._:-]{2,127}$"},
        "capabilityId":{"type":"string","enum":caps},
        "vendor":{"type":"string","enum":vendors},
        "environment":{"type":"string","minLength":1},
        "topology":{"type":"string","minLength":1},
        "operator":{"type":"string","minLength":1},
        "reason":{"type":"string","minLength":1},
        "approvalReference":{"type":"string","minLength":1},
        "approvedBy":{"type":"string","minLength":1},
        "startedAt":{"type":"string","format":"date-time"},
        "finishedAt":{"type":"string","format":"date-time"},
        "resultStatus":{"type":"string","enum":states},
        "reconcileRequired":{"type":"boolean"},
        "sourceSha":{"type":"string","pattern":"^[0-9a-fA-F]{40}$"},
        "evidenceSha256":{"type":"string","pattern":"^[0-9a-fA-F]{64}$"},
        "metrics":{"$ref":"#/components/schemas/DbOperationMetrics"},
        "trace":{"$ref":"#/components/schemas/DbOperationTrace"},
        "health":{"$ref":"#/components/schemas/DbOperationHealth"},
        "alerts":{"type":"array","items":{"type":"string","minLength":1}},
        "runbookRef":{"type":"string","minLength":1}
      }
    }
    spec={
      "openapi":"3.1.0",
      "info":{"title":"CPF DB Operations Provider Contract","version":str(contract["schemaVersion"])},
      "x-cpf-owner":contract["owner"],
      "x-cpf-provider-status":"DB_PROVIDER_CONTRACT_IMPLEMENTED",
      "x-cpf-route-consumer-status":"CROSS_SESSION_REQUIRED",
      "paths":{
        "/cpf/db/operations/verify":{
          "post":{
            "operationId":"verifyCpfDbOperationEvidence",
            "summary":"Validate and normalize CPF database operation evidence",
            "requestBody":{"required":True,"content":{"application/json":{"schema":{"$ref":"#/components/schemas/DbOperationEvidence"}}}},
            "responses":{
              "200":{"description":"Validated evidence","content":{"application/json":{"schema":{"$ref":"#/components/schemas/DbOperationEvidence"}}}},
              "400":{"description":"Invalid or incomplete evidence"},
              "403":{"description":"Authorization or separation-of-duties failure"},
              "409":{"description":"UNKNOWN or reconciliation conflict"}
            }
          }
        },
        "/cpf/db/operations/{operationId}":{
          "get":{
            "operationId":"getCpfDbOperationEvidence",
            "summary":"Read normalized DB operation evidence",
            "parameters":[{"name":"operationId","in":"path","required":True,"schema":{"type":"string"}}],
            "responses":{"200":{"description":"Operation evidence"},"404":{"description":"Operation not found"}}
          }
        }
      },
      "components":{"schemas":{
        "DbOperationEvidence":operation,
        "DbOperationMetrics":{"type":"object","required":["durationMs","affectedRows","errorCount","retryCount"],"additionalProperties":False,
          "properties":{k:{"type":"number","minimum":0} for k in ["durationMs","affectedRows","errorCount","retryCount"]}},
        "DbOperationTrace":{"type":"object","required":["traceId","spanId"],"additionalProperties":False,
          "properties":{"traceId":{"type":"string","pattern":"^[0-9a-fA-F]{16,32}$"},"spanId":{"type":"string","pattern":"^[0-9a-fA-F]{16}$"}}},
        "DbOperationHealth":{"type":"object","required":["before","after"],"additionalProperties":False,
          "properties":{"before":{"type":"string","enum":contract["publicContract"]["healthStates"]},"after":{"type":"string","enum":contract["publicContract"]["healthStates"]}}}
      }}
    }
    out=Path(a.output); out.parent.mkdir(parents=True,exist_ok=True)
    out.write_bytes((json.dumps(spec,ensure_ascii=False,indent=2,sort_keys=True)+"\n").encode("utf-8"))
    return 0
if __name__=="__main__":
    raise SystemExit(main())
