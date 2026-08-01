#!/usr/bin/env python3
"""Validate the CPF one-pass final verification plan before any expensive command runs."""
from __future__ import annotations
import argparse, json, re, sys
from collections import Counter
from pathlib import Path

VALID_RUNNERS={"python","node","pwsh","gradle","npm"}
REQUIRED_FLAGS={
  "cpf-tools/scripts/verify-cpf-openapi-controller-coverage.py": {"--root","--module","--openapi"},
  "cpf-tools/scripts/verify-cpf-requirement-traceability.py": {"--root","--expected-sha"},
  "cpf-tools/scripts/verify-cpf-controller-permission-contract.py": {"--root","--strict"},
  "cpf-tools/scripts/verify-cpf-adm-e2e-contract.py": {"--root"},
}
TOKEN_RE=re.compile(r"\{(root|sha|evidence)\}")
class PlanError(RuntimeError): pass

def load(path:Path)->dict:
    if not path.is_file(): raise PlanError(f"plan missing: {path}")
    try: data=json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc: raise PlanError(f"invalid plan JSON: {exc}") from exc
    if not isinstance(data,dict): raise PlanError("plan must be an object")
    return data

def validate(root:Path, data:dict, check_paths:bool=True)->tuple[int,int]:
    stages=data.get("orderedStages")
    commands=data.get("commands")
    if not isinstance(stages,list) or not stages or len(stages)!=len(set(stages)):
        raise PlanError("orderedStages must be a non-empty unique list")
    if not isinstance(commands,list) or not commands: raise PlanError("commands must be non-empty")
    ids=[]; seen_stage=-1; required=0
    for index,command in enumerate(commands,1):
        if not isinstance(command,dict): raise PlanError(f"command #{index} must be an object")
        for key in ("id","stage","runner","path","args","required"):
            if key not in command: raise PlanError(f"command #{index} missing {key}")
        command_id=command["id"]
        if not isinstance(command_id,str) or not re.fullmatch(r"[a-z0-9][a-z0-9-]*",command_id):
            raise PlanError(f"invalid command id: {command_id!r}")
        ids.append(command_id)
        stage=command["stage"]
        if stage not in stages: raise PlanError(f"{command_id}: unknown stage={stage}")
        stage_index=stages.index(stage)
        if stage_index < seen_stage: raise PlanError(f"{command_id}: command order regressed to stage={stage}")
        seen_stage=stage_index
        runner=command["runner"]
        if runner not in VALID_RUNNERS: raise PlanError(f"{command_id}: invalid runner={runner}")
        if command["required"] is not True: raise PlanError(f"{command_id}: final plan commands must be required")
        required+=1
        args=command["args"]
        if not isinstance(args,list) or not all(isinstance(value,str) for value in args):
            raise PlanError(f"{command_id}: args must be string list")
        for optional in ("preArgs","releaseArgs","requiredEnvironment"):
            if optional in command and (not isinstance(command[optional],list) or not all(isinstance(value,str) for value in command[optional])):
                raise PlanError(f"{command_id}: {optional} must be string list")
        required_flags=REQUIRED_FLAGS.get(command["path"],set())
        missing_flags=sorted(flag for flag in required_flags if flag not in args)
        if missing_flags: raise PlanError(f"{command_id}: required flags missing={missing_flags}")
        rendered=" ".join([command["path"],*args,*command.get("preArgs",[]),*command.get("releaseArgs",[])])
        leftovers=re.findall(r"\{[^}]+\}",rendered)
        invalid=[token for token in leftovers if not TOKEN_RE.fullmatch(token)]
        if invalid: raise PlanError(f"{command_id}: unsupported placeholders={invalid}")
        path=command["path"]
        if check_paths and runner not in {"npm"}:
            local=root/path
            if runner=="gradle" and not local.is_file():
                alternative=root/"gradlew"
                if not alternative.is_file(): raise PlanError(f"{command_id}: executable missing: {path}")
            elif runner!="gradle" and not local.is_file():
                raise PlanError(f"{command_id}: script missing: {path}")
    duplicates=[key for key,count in Counter(ids).items() if count>1]
    if duplicates: raise PlanError(f"duplicate command ids={duplicates}")
    covered={command["stage"] for command in commands}
    missing=[stage for stage in stages if stage not in covered]
    if missing: raise PlanError(f"stages without commands={missing}")
    return len(stages),required

def main()->int:
    parser=argparse.ArgumentParser()
    parser.add_argument("--root",type=Path,default=Path.cwd())
    parser.add_argument("--plan",default="cpf-tools/verification/20260801_01/cpf-final-verification-plan.json")
    parser.add_argument("--skip-path-check",action="store_true")
    args=parser.parse_args();root=args.root.resolve();data=load(root/args.plan)
    stages,commands=validate(root,data,not args.skip_path_check)
    print(f"[PASS] CPF final plan stages={stages} commands={commands} preflightOnly=true")
    return 0
if __name__=="__main__":
    try: raise SystemExit(main())
    except PlanError as error:
        print(f"[FAIL] {error}",file=sys.stderr);raise SystemExit(1)
