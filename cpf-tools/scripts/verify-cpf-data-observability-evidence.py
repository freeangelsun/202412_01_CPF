#!/usr/bin/env python3
"""Fail-closed validator for lineage, data-quality and reconciliation evidence."""
from __future__ import annotations
import argparse, hashlib, json, re
from pathlib import Path
from typing import Any
HEX40=re.compile(r"^[0-9a-f]{40}$",re.I); HEX64=re.compile(r"^[0-9a-f]{64}$",re.I)
SECRET=re.compile(r"password|secret|credential|access.?token|private.?key",re.I)

def load(path:Path)->dict[str,Any]:
 value=json.loads(path.read_text(encoding="utf-8-sig"))
 if not isinstance(value,dict): raise ValueError(f"JSON root must be object: {path}")
 return value

def sha(path:Path)->str:
 return hashlib.sha256(path.read_bytes()).hexdigest()

def secret_paths(value:Any,path:str="$")->list[str]:
 out=[]
 if isinstance(value,dict):
  for k,v in value.items():
   child=f"{path}.{k}"; out += [child] if SECRET.search(str(k)) else []; out += secret_paths(v,child)
 elif isinstance(value,list):
  for i,v in enumerate(value): out += secret_paths(v,f"{path}[{i}]")
 return out

def required(obj:dict[str,Any], fields:list[str], prefix:str, reasons:list[str])->None:
 for field in fields:
  if field not in obj or obj[field] in (None,""): reasons.append(f"{prefix}.{field} is required")

def evaluate(policy:dict[str,Any], evidence:dict[str,Any])->dict[str,Any]:
 reasons=[]; controls=policy["executionControls"]
 if evidence.get("vendor") not in policy["officialVendors"]: reasons.append("unsupported vendor")
 if not HEX40.fullmatch(str(evidence.get("sourceSha",""))): reasons.append("sourceSha must be exact 40-hex SHA")
 if not str(evidence.get("operationId","")).strip(): reasons.append("operationId is required")
 if len(str(evidence.get("reason","")).strip())<10: reasons.append("reason must contain at least 10 characters")
 if not str(evidence.get("operator","")).strip() or not str(evidence.get("approvedBy","")).strip(): reasons.append("operator and approvedBy are required")
 if evidence.get("operator")==evidence.get("approvedBy"): reasons.append("independent approval is required")
 if evidence.get("sanitized") is not True: reasons.append("sanitized must be true")
 leaks=secret_paths(evidence)
 if leaks: reasons.append("secret-bearing evidence keys are prohibited: "+",".join(leaks))
 state=evidence.get("state")
 if state not in policy["stateModel"]: reasons.append("invalid state")
 if state in {"PARTIAL","UNKNOWN"} and not str(evidence.get("reconcilePlan","")).strip(): reasons.append("reconcilePlan is required for partial/unknown")

 lineage=evidence.get("lineage")
 if not isinstance(lineage,dict): reasons.append("lineage must be object")
 else:
  nodes=lineage.get("nodes"); edges=lineage.get("edges")
  if not isinstance(nodes,list) or len(nodes)<policy["lineage"]["minimumNodeCount"]: reasons.append("lineage.nodes is below minimum") ; nodes=[] if not isinstance(nodes,list) else nodes
  if not isinstance(edges,list) or len(edges)<policy["lineage"]["minimumEdgeCount"]: reasons.append("lineage.edges is below minimum") ; edges=[] if not isinstance(edges,list) else edges
  node_ids=set()
  for i,node in enumerate(nodes):
   if not isinstance(node,dict): reasons.append(f"lineage.nodes[{i}] must be object"); continue
   required(node,policy["lineage"]["requiredNodeFields"],f"lineage.nodes[{i}]",reasons)
   if node.get("objectType") not in policy["lineage"]["allowedObjectTypes"]: reasons.append(f"lineage.nodes[{i}].objectType invalid")
   if not HEX64.fullmatch(str(node.get("schemaHash",""))): reasons.append(f"lineage.nodes[{i}].schemaHash invalid")
   if node.get("nodeId") in node_ids: reasons.append(f"duplicate lineage nodeId: {node.get('nodeId')}")
   node_ids.add(node.get("nodeId"))
  edge_ids=set()
  for i,edge in enumerate(edges):
   if not isinstance(edge,dict): reasons.append(f"lineage.edges[{i}] must be object"); continue
   required(edge,policy["lineage"]["requiredEdgeFields"],f"lineage.edges[{i}]",reasons)
   if edge.get("operation") not in policy["lineage"]["allowedOperations"]: reasons.append(f"lineage.edges[{i}].operation invalid")
   if edge.get("sourceNodeId") not in node_ids or edge.get("targetNodeId") not in node_ids: reasons.append(f"lineage.edges[{i}] references unknown node")
   if not HEX64.fullmatch(str(edge.get("mappingHash",""))): reasons.append(f"lineage.edges[{i}].mappingHash invalid")
   if edge.get("edgeId") in edge_ids: reasons.append(f"duplicate lineage edgeId: {edge.get('edgeId')}")
   edge_ids.add(edge.get("edgeId"))

 quality=evidence.get("quality")
 if not isinstance(quality,dict): reasons.append("quality must be object")
 else:
  rules=quality.get("rules"); results=quality.get("results")
  if not isinstance(rules,list) or not rules: reasons.append("quality.rules must be non-empty"); rules=[]
  if not isinstance(results,list) or not results: reasons.append("quality.results must be non-empty"); results=[]
  rule_ids=set()
  for i,rule in enumerate(rules):
   if not isinstance(rule,dict): reasons.append(f"quality.rules[{i}] must be object"); continue
   required(rule,policy["quality"]["requiredRuleFields"],f"quality.rules[{i}]",reasons)
   if rule.get("severity") not in policy["quality"]["severityLevels"]: reasons.append(f"quality.rules[{i}].severity invalid")
   if rule.get("thresholdType") not in policy["quality"]["allowedThresholdTypes"]: reasons.append(f"quality.rules[{i}].thresholdType invalid")
   if rule.get("ruleId") in rule_ids: reasons.append(f"duplicate quality ruleId: {rule.get('ruleId')}")
   rule_ids.add(rule.get("ruleId"))
  for i,result in enumerate(results):
   if not isinstance(result,dict): reasons.append(f"quality.results[{i}] must be object"); continue
   required(result,["ruleId","status","invalidCount","totalCount"],f"quality.results[{i}]",reasons)
   if result.get("ruleId") not in rule_ids: reasons.append(f"quality.results[{i}] references unknown rule")
   if result.get("status") not in ["PASS","FAIL","UNKNOWN"]: reasons.append(f"quality.results[{i}].status invalid")
   if result.get("status") != "PASS": reasons.append(f"quality result must be PASS: {result.get('ruleId')}")

 rec=evidence.get("reconciliation")
 if not isinstance(rec,dict): reasons.append("reconciliation must be object")
 else:
  comparisons=rec.get("comparisons")
  if not isinstance(comparisons,list) or not comparisons: reasons.append("reconciliation.comparisons must be non-empty"); comparisons=[]
  for i,c in enumerate(comparisons):
   if not isinstance(c,dict): reasons.append(f"reconciliation.comparisons[{i}] must be object"); continue
   required(c,policy["reconciliation"]["requiredComparisonFields"],f"reconciliation.comparisons[{i}]",reasons)
   for field in ("leftHash","rightHash"):
    if not HEX64.fullmatch(str(c.get(field,""))): reasons.append(f"reconciliation.comparisons[{i}].{field} invalid")
   for field in ("leftCount","rightCount","mismatchCount"):
    if not isinstance(c.get(field),int) or c[field]<0: reasons.append(f"reconciliation.comparisons[{i}].{field} invalid")
   if c.get("mismatchCount") != 0 or c.get("leftCount") != c.get("rightCount") or c.get("leftHash") != c.get("rightHash"): reasons.append(f"reconciliation comparison must match: {c.get('comparisonId')}")
 return {"schemaVersion":1,"status":"PASS" if not reasons else "FAIL","vendor":evidence.get("vendor"),"operationId":evidence.get("operationId"),"reasons":reasons}

def main()->int:
 ap=argparse.ArgumentParser(); ap.add_argument("--policy",required=True); ap.add_argument("--evidence",required=True); ap.add_argument("--expected-evidence-sha256",required=True); ap.add_argument("--output")
 a=ap.parse_args(); ep=Path(a.evidence); actual=sha(ep); expected=a.expected_evidence_sha256.lower()
 result={"schemaVersion":1,"status":"FAIL","reasons":[f"evidence sha256 mismatch expected={expected} actual={actual}"]} if actual!=expected else evaluate(load(Path(a.policy)),load(ep))
 result["evidenceSha256"]=actual
 if a.output: Path(a.output).write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
 print(json.dumps(result,ensure_ascii=False,sort_keys=True)); return 0 if result["status"]=="PASS" else 1
if __name__=="__main__": raise SystemExit(main())
