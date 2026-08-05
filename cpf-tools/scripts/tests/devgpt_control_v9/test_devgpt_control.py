#!/usr/bin/env python3
from __future__ import annotations
import csv, hashlib, json, shutil, subprocess, sys, tempfile, unittest
from pathlib import Path

SCRIPT_DIR=Path(__file__).resolve().parents[2]/"devgpt-control-v9"
sys.path.insert(0,str(SCRIPT_DIR))
from devgpt_control_lib import choose_primary, read_split_index, topo_sort
from generate_development_requests import prepare_immutable_output, relative_campaign_root, relative_session_root, exact_cleanup_command

class ManagementTests(unittest.TestCase):
    def test_choose_primary_explicit(self):
        row={"requirement_id":"CPF-FR-000001","canonical_requirement_id":"ARCH-MSA","work_item_id":"WP-2"}
        candidates=[{"entity_id":"WP-1","axis":"CONTRACT_OWNERSHIP"},{"entity_id":"WP-2","axis":"VERIFICATION_EVIDENCE"}]
        primary,support,basis,score,manual=choose_primary(row,candidates,{"WP-1","WP-2"})
        self.assertEqual("WP-2",primary); self.assertEqual("EXPLICIT_SOURCE_MAPPING",basis); self.assertFalse(manual)
    def test_choose_primary_semantic(self):
        row={"acceptance_criteria":"audit masking secret security authorization 검증"}
        candidates=[
            {"entity_id":"WP-A","axis":"IMPLEMENTATION_CONSUMER","axis_title":"구현"},
            {"entity_id":"WP-B","axis":"OPERATIONS_SECURITY","axis_title":"보안 감사 마스킹"},
        ]
        primary,_,basis,score,_=choose_primary(row,candidates,{"WP-A","WP-B"})
        self.assertEqual("WP-B",primary); self.assertGreater(score,0)
    def test_topological_sort(self):
        order,cycles=topo_sort(["A","B","C"],[("A","B"),("B","C")])
        self.assertEqual(["A","B","C"],order); self.assertEqual([],cycles)

    def test_immutable_campaign_output(self):
        with tempfile.TemporaryDirectory() as td:
            out=Path(td)/"campaign"/"REV-001"
            prepare_immutable_output(out)
            (out/"existing.txt").write_text("preserve",encoding="utf-8")
            with self.assertRaises(RuntimeError):
                prepare_immutable_output(out)

    def test_session_output_is_versioned_and_cleanup_is_exact(self):
        path=relative_session_root("cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9","DEV-20260805-R01","DEV-CPF_CORE-001",2)
        self.assertEqual(
            "cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/DEV-20260805-R01/REV-002/sessions/DEV-CPF_CORE-001",
            path
        )
        command=exact_cleanup_command(path)
        self.assertIn('Test-Path -LiteralPath',command)
        self.assertIn(path,command)
        self.assertNotIn("git clean",command)


    def test_campaign_root_is_inside_v9_namespace(self):
        path=relative_campaign_root("cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9","DEV-20260805-R01",3)
        self.assertEqual(
            "cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/DEV-20260805-R01/REV-003",
            path
        )
        self.assertNotIn("CPF_DEVELOPMENT_MANAGEMENT_V8",path)
        self.assertNotIn("development-session-results",path)

    def test_campaign_and_session_share_one_cleanup_root(self):
        campaign=relative_campaign_root("cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9","CAMP-1",1)
        session=relative_session_root("cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9","CAMP-1","SESSION-1",1)
        self.assertTrue(session.startswith(campaign + "/sessions/"))
        command=exact_cleanup_command(campaign)
        self.assertIn(campaign,command)
        self.assertNotIn("*",command)

    def test_split_dataset_validation(self):
        with tempfile.TemporaryDirectory() as td:
            root=Path(td); part=root/"part.csv"
            part.write_text("id,value\n1,a\n2,b\n",encoding="utf-8")
            sha=hashlib.sha256(part.read_bytes()).hexdigest()
            index=root/"index.csv"
            with index.open("w",encoding="utf-8-sig",newline="") as h:
                w=csv.DictWriter(h,fieldnames=["logical_record_count","part_sequence","part_path","part_record_count","sha256"]); w.writeheader(); w.writerow({"logical_record_count":2,"part_sequence":1,"part_path":"part.csv","part_record_count":2,"sha256":sha})
            rows,meta=read_split_index(index,root)
            self.assertEqual(2,len(rows)); self.assertEqual(2,meta["logical_rows"])
if __name__=="__main__": unittest.main()
