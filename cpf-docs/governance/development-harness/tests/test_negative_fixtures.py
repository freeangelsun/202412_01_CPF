#!/usr/bin/env python3
from pathlib import Path
import csv,json,tempfile,shutil,subprocess,sys,os
ROOT=Path(__file__).resolve().parents[4]; H=ROOT/'cpf-docs/governance/development-harness'
c=json.loads((H/'contracts/contract-registry.json').read_text(encoding='utf-8'))
checks=[]

# Windows parent Python may use cp949 even when the child correctly emits UTF-8.
# Every harness mutation subprocess is therefore decoded explicitly; otherwise a
# diagnostic decode failure can hide the negative mutation's real failure reason.
def _combined_output(cp): return (cp.stdout or '')+(cp.stderr or '')

_NEG_GROUP=os.environ.get('CPF_HARNESS_NEGATIVE_GROUP','ALL').strip().upper() or 'ALL'
_NEG_GROUPS={
    'BASE': {
        'mutation_empty_requirement_registry','mutation_self_migration','mutation_role_false_pass','mutation_vscode_nonzero',
    },
    'AUTH_A': {
        'mutation_drop_single_canonical_requirement','mutation_product_contract_semantic_damage','mutation_canonical_trace_loss',
        'mutation_current_authority_source_identity_drift','mutation_control_false_pass_without_evidence',
    },
    'AUTH_B': {
        'mutation_harness_package_garbage','mutation_current_package_projection_stale','mutation_handover_registry_alias_loss',
        'mutation_repository_python_cache_reentry',
        'mutation_transitive_migration_terminal_missing','mutation_deprecated_active_reference_reentry',
    },
    'STRENGTH': {
        'mutation_harness_strength_tracking_reduction','mutation_harness_strength_evidence_reduction',
        'mutation_protected_retain_delete_reentry','mutation_session_manifest_missing',
        'mutation_session_manifest_evidence_missing','mutation_session_manifest_not_available_declaration_incomplete',
        'mutation_toolchain_exact_host_patch_reentry','mutation_toolchain_exact_java_host_major_reentry',
        'mutation_public_source_test_launcher_removed','mutation_public_projection_template_launcher_removed',
        'mutation_generated_domain_test_launcher_removed','public_launcher_contract_positive_control',
        'runtime_startability_contract_positive_control','mutation_runtime_logging_resource_removed',
        'mutation_runtime_database_role_declaration_removed',
        'mutation_table_lookup_identifier_case_removed',
        'public_gradle_idempotency_contract_positive_control',
        'mutation_public_launcher_shared_project_cache_reentry',
        'mutation_public_root_settings_project_cache_reentry',
        'mutation_ide_separate_project_cache_reentry',
        'mutation_release_cleanup_before_prerequisite_check',
        'consumer_regression_contract_positive_control',
        'mutation_finding_ledger_collapsed_into_one',
        'mutation_public_runtime_lifecycle_delegates_internal_engine',
        'mutation_security_common_crypto_service_unregistered',
        'mutation_runtime_environment_db_vendor_removed',
        'mutation_public_vendor_pack_projection_removed',
        'mutation_bootstrap_local_secret_generation_removed',
        'mutation_platform_role_primary_autoconfiguration_unregistered',
        'mutation_injection_constructor_annotation_removed',
        'mutation_platform_runtime_service_identity_removed',
        'mutation_adm_bootstrap_runner_context_scope_removed',
        'mutation_control_plane_management_root_paths_removed',
        'mutation_generated_key_columns_removed',
        'mutation_control_plane_adm_root_path_unlisted',
        'mutation_local_profile_secure_cookie_reentry',
        'mutation_domain_runtime_platform_db_role_removed',
        'mutation_domain_persistence_declaration_removed',
        'mutation_security_common_jwt_service_unregistered',
        'mutation_transactional_bean_made_final',
        'mutation_cpf_service_base_class_removed',
        'mutation_domain_persistence_provider_mismatch',
        'mutation_runtime_instance_id_assignment_removed',
        'mutation_runtime_role_value_out_of_contract',
        'mutation_prebuilt_domain_ddl_projection_removed',
        'mutation_canonical_seed_conflict_target_invalid',
        'mutation_channel_front_object_mapper_removed',
        'mutation_initial_operator_predicate_counts_credentialless',
        'mutation_source_runtime_uses_bootrun',
        'mutation_mbw_bootstrap_runner_context_scope_removed',
        'mutation_runtime_security_chain_removed',
        'mutation_domain_runtime_started_by_bootrun',
        'mutation_initial_operator_forced_password_change',
        'mutation_runtime_role_namespaces_merged',
    },
    'RUNTIME_CLI': {
        'runtime_lifecycle_cli_contract_positive_control',
        'mutation_runtime_group_authority_removed',
        'mutation_runtime_group_membership_hardcoded',
        'mutation_group_name_collides_with_target_name',
        'mutation_generated_domain_target_name_module_qualified',
        'mutation_generated_domain_name_hardcoded_in_cli',
        'mutation_runtime_target_list_duplicated_in_cli',
        'mutation_new_batch_runtime_missing_from_batch_group',
        'mutation_all_group_narrowed_to_a_subset',
        'mutation_runtime_dependency_cycle_introduced',
        'mutation_channel_front_dependency_removed',
        'mutation_group_partial_failure_reported_as_pass',
        'mutation_group_status_healthy_from_pid_alone',
        'mutation_stop_keeps_start_order',
        'mutation_unsupported_capability_silently_passed',
        'mutation_launcher_verb_parity_broken',
        'mutation_powershell_wrapper_target_translation_removed',
        'mutation_launcher_reimplements_command_parsing',
        'mutation_readme_command_not_in_cli',
        'mutation_public_runtime_left_undocumented',
        'mutation_public_runtime_not_obtainable',
        'mutation_lifecycle_verb_not_public',
        'mutation_harness_lifecycle_cli_rule_removed',
        'mutation_registry_validator_relation_removed',
    },
    'RELEASE_ASSET': {
        'release_asset_freshness_contract_positive_control',
        'mutation_release_asset_authority_removed',
        'mutation_produced_asset_declaration_removed',
        'mutation_produced_asset_becomes_release_input',
        'mutation_release_asset_axis_declaration_removed',
        'mutation_release_asset_classification_unmapped',
        'mutation_release_asset_classified_by_path_name',
        'mutation_release_asset_classified_by_extension',
        'mutation_tracked_result_becomes_release_input',
        'mutation_tracked_result_exempted_from_fresh_regeneration',
        'mutation_public_surface_inferred_from_master_tracking',
        'mutation_untracked_result_forced_out_of_public_release',
        'mutation_tracking_exception_reason_removed',
        'mutation_tracking_exception_evidence_reduced',
        'mutation_size_threshold_hardcoded',
        'mutation_canonical_source_regenerated_by_release_engine',
        'mutation_canonical_source_reuses_previous_release_output',
        'mutation_canonical_source_verification_reduced',
        'mutation_launcher_reclassified_as_tracked_result',
        'mutation_release_engine_generates_launcher_body',
        'mutation_release_engine_classifies_by_extension',
        'mutation_generator_input_declaration_removed',
        'mutation_promotion_before_verification',
        'mutation_artifact_set_currentized_non_atomically',
        'mutation_release_clean_uses_git_clean',
        'mutation_release_clean_protects_no_repository_authority',
        'mutation_previous_release_residue_allowed',
        'mutation_release_destroys_before_prerequisites',
        'mutation_engine_generates_before_cleaning_release_root',
        'mutation_engine_reads_previous_release_as_input',
        'mutation_engine_publishes_outside_isolated_staging',
        'mutation_publisher_targets_open_git_tree',
        'mutation_gitignore_blanket_excludes_release_artifacts',
        'mutation_gitignore_excludes_release_metadata_too',
        'mutation_unknown_artifact_silently_allowed',
        'mutation_lfs_transport_not_adopted',
        'mutation_lfs_runtime_reclassified_as_regular_git',
        'mutation_lfs_global_jar_scope',
        'mutation_lfs_metadata_scope',
        'mutation_lfs_validator_removed',
        'mutation_engine_skips_fresh_open_git_lfs_gate',
        'mutation_lfs_size_threshold_replaces_measurement',
        'mutation_lfs_runtime_dropped_from_public_release',
        'mutation_payload_composition_rule_removed',
        'mutation_payload_tool_reports_only_total_size',
        'mutation_development_gate_forced_to_full_release',
        'mutation_final_order_projects_before_build',
        'mutation_single_acceptance_statement_allowed',
        'mutation_previous_release_copy_kept_in_working_tree',
        'mutation_harness_release_asset_rule_removed',
        'mutation_release_asset_registry_relation_removed',
    },
    'SERVICE_REGISTRY': {
        'service_registry_provisioning_contract_positive_control',
        'mutation_service_registry_contract_removed',
        'mutation_service_registry_owner_becomes_the_generator',
        'mutation_service_registry_required_column_unprovided',
        'mutation_service_registry_identity_inferred_from_domain_name',
        'mutation_service_registry_transform_allows_fallback',
        'mutation_service_registry_targets_enumerated_domains',
        'mutation_service_registry_conflict_overwrites_existing_row',
        'mutation_service_registry_disabled_row_silently_enabled',
        'mutation_service_registry_runtime_self_registration_allowed',
        'mutation_service_registry_rerun_not_idempotent',
        'mutation_service_registry_profile_specific_behaviour',
        'mutation_service_registry_vendor_sql_removed',
        'mutation_service_endpoint_provisioning_removed',
        'mutation_service_endpoint_code_diverges_from_runtime',
        'mutation_service_endpoint_scope_conflict_overwrites',
        'mutation_service_endpoint_reconcile_not_wired',
        'mutation_service_registry_sql_duplicated_in_bootstrap',
        'mutation_service_registry_reconcile_not_wired_into_bootstrap',
        'mutation_service_registry_domain_name_hardcoded_in_bootstrap',
        'mutation_service_registry_contract_not_projected_to_public',
        'mutation_harness_service_registry_rule_removed',
        'mutation_service_registry_registry_relation_removed',
    },
}
def _enabled(name):
    if _NEG_GROUP=='ALL': return True
    if _NEG_GROUP=='BASE': return not name.startswith('mutation_') or name in _NEG_GROUPS['BASE']
    return name in _NEG_GROUPS.get(_NEG_GROUP,set())

def record(name,ok,detail=''):
    if not _enabled(name): return
    checks.append((name,ok,detail)); print(('PASS' if ok else 'FAIL'),name,detail,flush=True)
record('forbid_not_executed','NOT_EXECUTED' in c['forbiddenCompletionSignals'])
record('forbid_unknown','UNKNOWN' in c['forbiddenCompletionSignals'])
record('forbid_fail','FAIL' in c['forbiddenCompletionSignals'])
record('profiles_nonempty',bool(c['environmentProfiles']))
record('db_vendors_nonempty',bool(c['officialDbVendors']))
record('roles_three_way',set(c['roles'])=={'DEVGPT','INDEPENDENT_REVIEWER','QA'})
record('codex_claude_same_role',{'Codex','Claude'}.issubset(set(c['roles']['INDEPENDENT_REVIEWER']['equivalentActors'])))
record('required_completion_evidence',len(c['requiredCompletionEvidence'])>=8)
record('required_test_evidence',len(c['requiredTestEvidence'])>=10)
record('reviewer_vscode_zero_gate_fields',set(c['independentReviewerSourceModificationEvidence']) >= {'source_modified','vscode_fresh_import','vscode_scope','vscode_problems_json','vscode_error_count','vscode_warning_count'})
with (H/'current/CANONICAL_PRODUCT_REQUIREMENTS.csv').open(encoding='utf-8-sig',newline='') as f: n=sum(1 for _ in csv.DictReader(f))
record('non_vacuous_product_registry',n>0)

# Session Merge Protocol negative mutation is exercised from a known-positive isolated fixture.
# This prevents an already-pending real session from making the mutation look green by accident.
def _session_merge_missing_manifest_mutation(mutate,expected):
    import hashlib
    root=Path(tempfile.mkdtemp(prefix='cpf-session-merge-negative-'))
    try:
        h=root/'cpf-docs/governance/development-harness'
        (h/'current').mkdir(parents=True)
        d=h/'evidence/claude/current/sessions/FIXTURE_SESSION'
        d.mkdir(parents=True)
        (h/'current/CURRENT_WORK_ITEM_REGISTRY.csv').write_text('work_item_id\nWP-H00\n',encoding='utf-8-sig')
        key='FIXTURE_SESSION'
        digest=hashlib.sha256(key.encode('utf-8')).hexdigest()
        (h/'CPF_DEVELOPMENT_HARNESS.md').write_text(
            '### Current Merge Control State\n\nstate: current/CURRENT_MERGE_CONTROL_STATE.json\n\n## 21. roles\n',
            encoding='utf-8'
        )
        (h/'current/CURRENT_MERGE_CONTROL_STATE.json').write_text(json.dumps({
            'merge_protocol_version':'1','merge_baseline_source_identity':'1'*64,'last_merged_session_key':key,
            'merged_session_set_digest':digest,'pending_session_keys':'NONE','conflict_session_keys':'NONE',
            'last_merge_review_at':'2026-08-30T14:00:00+09:00','last_merge_reviewer_session_key':'HARNESS_FIXTURE'
        },ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        report=d/'SESSION_REPORT.md'
        report.write_text('# Report\n\n## WI-WP-H00\n',encoding='utf-8')
        ev=d/'evidence.txt'
        ev.write_text('PASS\n',encoding='utf-8')
        rel=lambda x: x.relative_to(root).as_posix()
        manifest={
          'schemaVersion':1,'sessionKey':key,'role':'CLAUDE','startedAt':'2026-08-30T14:00:00+09:00','endedAt':'2026-08-30T14:01:00+09:00',
          'sourceIdentity':'1'*64,'sourceBasis':'fixture','registrySha256AtStart':'2'*64,
          'reportPath':rel(report),'reportSha256':hashlib.sha256(report.read_bytes()).hexdigest(),
          'workItems':[{'workItemId':'WP-H00','proposedStatus':'SOURCE_FIXED','evidence':[rel(ev)],'reportAnchor':'WI-WP-H00',
                        'acceptanceMapping':{'prerequisiteSource':'fixture','requiredEnvironment':'fixture','actualEnvironment':'fixture'}}],
          'evidenceFiles':[{'path':rel(ev),'sha256':hashlib.sha256(ev.read_bytes()).hexdigest(),'purpose':'fixture'}],
          'gitWriteExecuted':False,'mergeStatus':'MERGED','mergedBySessionKey':'HARNESS_FIXTURE','mergedAt':'2026-08-30T14:02:00+09:00','mergeTargetSourceIdentity':'1'*64,
          'pendingReasons':[],'conflicts':[],'rerunConditions':[]}
        mf=d/'SESSION_MANIFEST.json'
        mf.write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        env=os.environ.copy()
        env['CPF_HARNESS_ROOT']=str(h)
        env['CPF_REPOSITORY_ROOT']=str(root)
        validator=H/'validators/validate_session_merge_protocol.py'
        before=subprocess.run([sys.executable,'-B',str(validator)],cwd=ROOT,env=env,text=True,capture_output=True,encoding='utf-8',errors='replace')
        if before.returncode!=0: return False
        mutate(mf,ev,manifest)
        after=subprocess.run([sys.executable,'-B',str(validator)],cwd=ROOT,env=env,text=True,capture_output=True,encoding='utf-8',errors='replace')
        return after.returncode!=0 and expected in _combined_output(after)
    finally:
        shutil.rmtree(root,ignore_errors=True)

def _drop_manifest(mf,ev,manifest):
    mf.unlink()

# Manifest 가 참조하는 증적이 사라지면 기본은 fail-closed 다. 증적 없이 통과하면 "그 검증을
# 수행했다"는 기록만 남고 근거가 사라진다.
def _drop_evidence_file(mf,ev,manifest):
    ev.unlink()

# 부재를 인정하더라도 근거/재현성/승계 여부를 명시하지 않은 선언은 허용하지 않는다.
# NOT_AVAILABLE 은 PASS 가 아니라 "증적 없음"을 정확히 기록한 상태여야 한다.
def _declare_not_available_without_reason(mf,ev,manifest):
    import json as _json
    ev.unlink()
    manifest['evidenceFiles'][0]['evidence_status']='NOT_AVAILABLE'
    mf.write_text(_json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

for _name,_mutate,_expected,_detail in (
    ('mutation_session_manifest_missing',_drop_manifest,'MANIFEST_MISSING:FIXTURE_SESSION',
     'positive fixture -> missing manifest must fail closed'),
    ('mutation_session_manifest_evidence_missing',_drop_evidence_file,'MANIFEST_EVIDENCE_MISSING:FIXTURE_SESSION',
     'manifest evidence disappearance must fail closed'),
    ('mutation_session_manifest_not_available_declaration_incomplete',_declare_not_available_without_reason,
     'MANIFEST_EVIDENCE_NOT_AVAILABLE_DECLARATION_INVALID:FIXTURE_SESSION',
     'NOT_AVAILABLE without reason/reproducibility/inheritance must fail closed'),
):
    if _enabled(_name):
        record(_name,_session_merge_missing_manifest_mutation(_mutate,_expected),_detail)

# Executable Delete Manifest must contain only approved/delete-eligible paths. Protected-retain
# provenance lives only in Migration Map/Semantic Ledger so unrelated protected documentation
# changes cannot block or be targeted by legacy cleanup.
with (H/'DELETE_MANIFEST.csv').open(encoding='utf-8-sig',newline='') as f:
    _delete_rows=list(csv.DictReader(f))
_protected_prefixes=('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/','cpf-docs/governance/documentation-harness/')
record('delete_manifest_all_rows_executable', all(r.get('delete_eligible')=='true' and r.get('approved')=='true' and r.get('user_approved')=='true' for r in _delete_rows))
record('delete_manifest_protected_retain_zero', not any(r.get('path','').startswith(_protected_prefixes) for r in _delete_rows))

# 대용량 Current Dataset(약 220MB)을 mutation마다 byte-copy하면 로컬/CI에서 검증 자체가
# timeout될 수 있다. 검수 강도는 그대로 유지하고 동일 파일시스템에서는 hard-link clone을
# 사용한 뒤 mutation 대상 파일만 copy-on-write로 분리한다. hard-link가 지원되지 않으면
# shutil.copy2로 자동 fallback한다.
def _link_or_copy(src, dst):
    try:
        os.link(src, dst)
        return dst
    except OSError:
        return shutil.copy2(src, dst)

def _clone_harness(target: Path):
    shutil.copytree(H, target, copy_function=_link_or_copy)

def _detach(target: Path, *relative_paths: str):
    for rel in relative_paths:
        p=target/rel
        if not p.exists() or not p.is_file():
            continue
        data=p.read_bytes()
        p.unlink()
        p.write_bytes(data)

# Mutation tests use one reusable hard-link fixture. This preserves the exact mutation/validator
# strength while avoiding 220MB Harness tree traversal for every single negative case.
_MUTABLE_RELATIVE_PATHS=(
    'current/CANONICAL_PRODUCT_REQUIREMENTS.csv',
    'CANONICAL_MIGRATION_MAP.csv',
    'current/ROLE_EXECUTION_LEDGER.csv',
    'product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md',
    'current/CANONICAL_REQUIREMENT_TRACE.csv',
    'current/CURRENT_WORK_ITEM_REGISTRY.csv',
    'current/CONTROL_EXECUTION_LEDGER.csv',
    'current/PACKAGE_MANIFEST.json',
    'current/CPF_DEVELOPMENT_HANDOVER.md',
    'contracts/contract-registry.json',
    'DELETE_MANIFEST.csv',
)
_NEG_ROOT=Path(tempfile.mkdtemp(prefix='cpf-harness-negative-shared-'))
_NEG_TARGET=_NEG_ROOT/'cpf-docs/governance/development-harness'
_NEG_TARGET.parent.mkdir(parents=True,exist_ok=True)
_clone_harness(_NEG_TARGET)
# Strength validator also consumes the canonical toolchain compatibility policy outside Harness.
# Copy that single authority into the isolated negative fixture so mutations test the rule itself,
# not an unrelated missing-file condition.
_policy_src=ROOT/'cpf-tools/verification/contracts/cpf-toolchain-compatibility.json'
_policy_dst=_NEG_ROOT/'cpf-tools/verification/contracts/cpf-toolchain-compatibility.json'
_policy_dst.parent.mkdir(parents=True,exist_ok=True)
shutil.copy2(_policy_src,_policy_dst)

# migration replacements can point outside Harness; create only harmless placeholders required
# for the migration structure validator. They never count as product evidence.
with (_NEG_TARGET/'CANONICAL_MIGRATION_MAP.csv').open(encoding='utf-8-sig',newline='') as f:
    _NEG_MIGRATION_ROWS=list(csv.DictReader(f))
for _r in _NEG_MIGRATION_ROWS:
    _p=_NEG_ROOT/_r['new_path']; _p.parent.mkdir(parents=True,exist_ok=True)
    if not _p.exists() and not str(_p).startswith(str(_NEG_TARGET)+os.sep):
        _p.write_text('fixture',encoding='utf-8')

def _restore_negative_fixture():
    for rel in _MUTABLE_RELATIVE_PATHS:
        src=H/rel; dst=_NEG_TARGET/rel
        if not src.exists():
            if dst.exists():
                if dst.is_dir(): shutil.rmtree(dst)
                else: dst.unlink()
            continue
        dst.parent.mkdir(parents=True,exist_ok=True)
        if dst.exists():
            if dst.is_dir(): shutil.rmtree(dst)
            else: dst.unlink()
        shutil.copy2(src,dst)
    garbage=_NEG_TARGET/'.pytest_cache'
    if garbage.exists(): shutil.rmtree(garbage)
    deprecated_fixture=_NEG_ROOT/'cpf-tools/deprecated-reentry-fixture.py'
    if deprecated_fixture.exists(): deprecated_fixture.unlink()

def run_mut(name, mutate, expected_fragment):
    if not _enabled(name): return
    _restore_negative_fixture()
    mutate(_NEG_TARGET)
    cp=subprocess.run([sys.executable,'-B',str(_NEG_TARGET/'validators/validate_development_harness.py')],cwd=_NEG_ROOT,text=True,capture_output=True,encoding='utf-8',errors='replace')
    ok=cp.returncode!=0 and expected_fragment in _combined_output(cp)
    record(name,ok,('rc='+str(cp.returncode)+' expected='+expected_fragment))

def mut_empty_req(h):
    p=h/'current/CANONICAL_PRODUCT_REQUIREMENTS.csv'; data=p.read_bytes(); p.unlink(); p.write_bytes(data)
    with p.open(encoding='utf-8-sig',newline='') as f: hdr=next(csv.reader(f))
    with p.open('w',encoding='utf-8-sig',newline='') as f: csv.writer(f).writerow(hdr)
run_mut('mutation_empty_requirement_registry',mut_empty_req,'PRODUCT_REQUIREMENT_REGISTRY_EMPTY_OR_DUPLICATE')

def mut_self_migration(h):
    p=h/'CANONICAL_MIGRATION_MAP.csv'; data=p.read_bytes(); p.unlink(); p.write_bytes(data)
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr[0]['new_path']=rr[0]['old_path']
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_mut('mutation_self_migration',mut_self_migration,'SELF_MIGRATION')

def mut_false_pass(h):
    p=h/'current/ROLE_EXECUTION_LEDGER.csv'; data=p.read_bytes(); p.unlink(); p.write_bytes(data)
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr[0]['execution_status']='PASS'; rr[0]['completion_reason']=''; rr[0]['command']=''; rr[0]['evidence']=''
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_mut('mutation_role_false_pass',mut_false_pass,'FALSE_PASS')

def mut_vscode_false_pass(h):
    p=h/'current/ROLE_EXECUTION_LEDGER.csv'; data=p.read_bytes(); p.unlink(); p.write_bytes(data)
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    x=next(r for r in rr if r['role']=='INDEPENDENT_REVIEWER')
    for k in c['requiredCompletionEvidence']: x[k]='fixture'
    x['execution_status']='PASS';x['source_modified']='true';x['vscode_fresh_import']='true';x['vscode_scope']='all';x['vscode_problems_json']='fixture.json';x['vscode_error_count']='1';x['vscode_warning_count']='0'
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_mut('mutation_vscode_nonzero',mut_vscode_false_pass,'FALSE_PASS_VSCODE_DIAGNOSTIC')

print(f'NEGATIVE_FIXTURES_BASE={sum(1 for x in checks if x[1])}/{len(checks)} PASS')

# Authority/semantic negative mutations: the Harness must fail even when its basic structure still looks valid.
def run_auth_mut(name, mutate, expected_fragment):
    if not _enabled(name): return
    _restore_negative_fixture()
    mutate(_NEG_TARGET)
    cp=subprocess.run([sys.executable,'-B',str(_NEG_TARGET/'validators/validate_harness_authority.py')],cwd=_NEG_ROOT,text=True,capture_output=True,encoding='utf-8',errors='replace')
    ok=cp.returncode!=0 and expected_fragment in _combined_output(cp)
    record(name,ok,'rc='+str(cp.returncode)+' expected='+expected_fragment)

def mut_drop_one_canonical(h):
    p=h/'current/CANONICAL_PRODUCT_REQUIREMENTS.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr=rr[1:]
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_auth_mut('mutation_drop_single_canonical_requirement',mut_drop_one_canonical,'PRODUCT_REQUIREMENT_SET_DRIFT')

def mut_product_text(h):
    p=h/'product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md'; t=p.read_text(encoding='utf-8'); p.write_text(t.replace('CPF를 샘플이나 공통 라이브러리가 아닌','CPF를 임시 샘플 프레임워크로',1),encoding='utf-8')
run_auth_mut('mutation_product_contract_semantic_damage',mut_product_text,'PRODUCT_CONTRACT_UNAPPROVED_DRIFT')

def mut_trace_drop(h):
    p=h/'current/CANONICAL_REQUIREMENT_TRACE.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr[:-1])
run_auth_mut('mutation_canonical_trace_loss',mut_trace_drop,'CANONICAL_TRACE_SET_DRIFT')

def mut_identity(h):
    p=h/'current/CURRENT_WORK_ITEM_REGISTRY.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr[0]['source_identity']='0'*64
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_auth_mut('mutation_current_authority_source_identity_drift',mut_identity,'AUTHORITY_SOURCE_IDENTITY_DRIFT')

def mut_control_false_pass(h):
    p=h/'current/CONTROL_EXECUTION_LEDGER.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr[0]['status']='PASS';rr[0]['evidence']=''
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_auth_mut('mutation_control_false_pass_without_evidence',mut_control_false_pass,'CONTROL_FALSE_PASS')

def mut_harness_garbage(h):
    p=h/'.pytest_cache';p.mkdir();(p/'garbage.txt').write_text('x',encoding='utf-8')
run_auth_mut('mutation_harness_package_garbage',mut_harness_garbage,'HARNESS_GARBAGE')


def mut_repository_python_cache(h):
    # GARBAGE-0637: regeneratable Python bytecode 는 Harness payload 밖에서도 product source 에 남으면 안 된다.
    d=_NEG_ROOT/'cpf-tools/__pycache__'; d.mkdir(parents=True,exist_ok=True)
    (d/'stale_module.cpython-313.pyc').write_bytes(b'cpf-stale-bytecode')
run_auth_mut('mutation_repository_python_cache_reentry',mut_repository_python_cache,'REPOSITORY_PYTHON_CACHE')


def mut_package_projection_stale(h):
    p=h/'current/PACKAGE_MANIFEST.json'; data=json.loads(p.read_text(encoding='utf-8')); data['currentSourceIdentity']='0'*64; p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
run_auth_mut('mutation_current_package_projection_stale',mut_package_projection_stale,'CURRENT_PACKAGE_SOURCE_IDENTITY_STALE')

def mut_handover_registry_alias_loss(h):
    p=h/'current/CURRENT_WORK_ITEM_REGISTRY.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    for r in rr:
        aliases=[x for x in r.get('handover_aliases','').split(';') if x and x!='WP-R01.21']
        r['handover_aliases']=';'.join(aliases)
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_auth_mut('mutation_handover_registry_alias_loss',mut_handover_registry_alias_loss,'HANDOVER_REGISTRY_CONSISTENCY')

def mut_transitive_migration_terminal_missing(h):
    p=h/'CANONICAL_MIGRATION_MAP.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr[0]['new_path']='cpf-docs/governance/development-harness/missing-terminal.md'
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_auth_mut('mutation_transitive_migration_terminal_missing',mut_transitive_migration_terminal_missing,'MIGRATION_TRANSITIVE_TERMINAL_MISSING')

def mut_deprecated_active_reference_reentry(h):
    root=h.parents[2]; p=root/'cpf-tools/deprecated-reentry-fixture.py'; p.parent.mkdir(parents=True,exist_ok=True); p.write_text("OLD='cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md'\n",encoding='utf-8')
run_auth_mut('mutation_deprecated_active_reference_reentry',mut_deprecated_active_reference_reentry,'DEPRECATED_ACTIVE_REFERENCE')


# Harness 현행화 자체도 regression 대상이다. 기존 범위/증거 강도를 낮추면 FAIL해야 한다.
def run_strength_mut(name, mutate, expected_fragment):
    if not _enabled(name): return
    _restore_negative_fixture()
    mutate(_NEG_TARGET)
    cp=subprocess.run([sys.executable,'-B',str(_NEG_TARGET/'validators/validate_harness_strength_regression.py')],cwd=_NEG_ROOT,text=True,capture_output=True,encoding='utf-8',errors='replace')
    ok=cp.returncode!=0 and expected_fragment in _combined_output(cp)
    record(name,ok,'rc='+str(cp.returncode)+' expected='+expected_fragment)

def mut_tracking_scope_reduced(h):
    p=h/'current/CURRENT_WORK_ITEM_REGISTRY.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    for i,r in enumerate(rr):
        if r.get('item_role','TRACKING')=='TRACKING': rr.pop(i); break
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_strength_mut('mutation_harness_strength_tracking_reduction',mut_tracking_scope_reduced,'TRACKING_WORK_REDUCED')

def mut_toolchain_exact_host_patch(h):
    root=h.parents[2]; p=root/'cpf-tools/verification/contracts/cpf-toolchain-compatibility.json'
    data=json.loads(p.read_text(encoding='utf-8'));data['tools']['npm']['exactPatchRequired']=True
    p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
run_strength_mut('mutation_toolchain_exact_host_patch_reentry',mut_toolchain_exact_host_patch,'HOST_EXACT_PATCH_PIN_REINTRODUCED')

def mut_toolchain_exact_java_host_major(h):
    root=h.parents[2]; p=root/'cpf-tools/verification/contracts/cpf-toolchain-compatibility.json'
    data=json.loads(p.read_text(encoding='utf-8'));data['tools']['java']['maxMajor']=25
    p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
run_strength_mut('mutation_toolchain_exact_java_host_major_reentry',mut_toolchain_exact_java_host_major,'HOST_JAVA_EXACT_MAJOR_PIN_REINTRODUCED')


def mut_test_evidence_reduced(h):
    p=h/'contracts/contract-registry.json'; data=json.loads(p.read_text(encoding='utf-8'))
    data['requiredTestEvidence']=data['requiredTestEvidence'][:-1]
    p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
run_strength_mut('mutation_harness_strength_evidence_reduction',mut_test_evidence_reduced,'HARNESS_STRENGTH_REDUCED requiredTestEvidence')

def mut_protected_retain_delete_reentry(h):
    p=h/'DELETE_MANIFEST.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr.append({
        'path':'cpf-docs/deliverables/PROTECTED_RETAIN_FIXTURE.md','type':'FILE','approved':'false','user_approved':'false',
        'precondition':'PROTECTED_PATH_RETAIN','lifecycle':'PROTECTED_RETAIN',
        'replacement_path':'cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md',
        'expected_sha256':'0'*64,'semantic_status':'PASS','delete_eligible':'false','reason':'negative fixture'
    })
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_mut('mutation_protected_retain_delete_reentry',mut_protected_retain_delete_reentry,'DELETE_MIGRATION_PATH_SET_MISMATCH')


# 공개 Source Development Surface 의 Test 실행 가능성 계약은 Harness 복제본이 아니라
# build.gradle / 공개 projection template / Domain Generator 라는 Harness 밖 정본을 본다.
# 그래서 이 계약만의 격리 fixture 를 따로 만든다. 실제 정본은 건드리지 않는다.
_PUBLIC_LAUNCHER_TEST=ROOT/'cpf-tools/verification/tests/test_cpf_public_test_launcher_contract.py'
_PUBLIC_LAUNCHER_FIXTURE_FILES=[
    'cpf-tools/governance/cpf-product-surface-policy.json',
    'cpf-tools/release/open-git/open-git-surface-policy.json',
    'cpf-tools/generator/engine/cpf_domain_generator.py',
    'cpf-tools/release/open-git/templates/cpf-education/build.gradle',
    'cpf-education/build.gradle',
]

def _public_launcher_fixture():
    root=Path(tempfile.mkdtemp(prefix='cpf-public-launcher-negative-'))
    for rel in _PUBLIC_LAUNCHER_FIXTURE_FILES:
        dst=root/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(ROOT/rel,dst)
    # 계약은 Test Source 존재 여부만 본다. 실제 Test 를 복사하지 않고 최소 표식만 둔다.
    marker=root/'cpf-education/src/test/java/CpfNegativeFixtureTest.java'
    marker.parent.mkdir(parents=True,exist_ok=True)
    marker.write_text('class CpfNegativeFixtureTest {}'+chr(10),encoding='utf-8')
    return root

def _run_public_launcher_contract(root):
    env=dict(os.environ)
    env['CPF_PUBLIC_SURFACE_ROOT']=str(root); env['PYTHONUTF8']='1'; env['PYTHONIOENCODING']='utf-8'
    return subprocess.run([sys.executable,'-B',str(_PUBLIC_LAUNCHER_TEST)],cwd=str(ROOT),
                          text=True,capture_output=True,encoding='utf-8',errors='replace',env=env)

def _strip_launcher(path):
    kept=[line for line in path.read_text(encoding='utf-8').splitlines(True)
          if 'junit-platform-launcher' not in line]
    path.write_text(''.join(kept),encoding='utf-8')

def run_public_launcher_mut(name,mutate,expected_fragment):
    if not _enabled(name): return
    root=_public_launcher_fixture()
    try:
        mutate(root)
        cp=_run_public_launcher_contract(root)
        ok=cp.returncode!=0 and expected_fragment in _combined_output(cp)
        record(name,ok,'rc='+str(cp.returncode)+' expected='+expected_fragment)
    finally:
        shutil.rmtree(root,ignore_errors=True)

# fixture 구성이 잘못되면 모든 mutation 이 통과한 것처럼 보인다. 무손상 fixture 는 반드시 PASS 여야 한다.
def _public_launcher_positive_control():
    if not _enabled('public_launcher_contract_positive_control'): return
    root=_public_launcher_fixture()
    try:
        cp=_run_public_launcher_contract(root)
        record('public_launcher_contract_positive_control',cp.returncode==0,'rc='+str(cp.returncode))
    finally:
        shutil.rmtree(root,ignore_errors=True)
_public_launcher_positive_control()

def mut_public_project_launcher_removed(root):
    _strip_launcher(root/'cpf-education/build.gradle')
run_public_launcher_mut('mutation_public_source_test_launcher_removed',mut_public_project_launcher_removed,
                        'junit-platform-launcher 미선언 공개 Project')

def mut_public_template_launcher_removed(root):
    _strip_launcher(root/'cpf-tools/release/open-git/templates/cpf-education/build.gradle')
run_public_launcher_mut('mutation_public_projection_template_launcher_removed',mut_public_template_launcher_removed,
                        'junit-platform-launcher 미선언 공개 template')

def mut_generator_launcher_removed(root):
    _strip_launcher(root/'cpf-tools/generator/engine/cpf_domain_generator.py')
run_public_launcher_mut('mutation_generated_domain_test_launcher_removed',mut_generator_launcher_removed,
                        'junit-platform-launcher 를 선언하지 않는다')


# 발행된 Runtime 의 단독 기동 계약과 공개 Workspace 의 Gradle 반복 실행 계약은 Harness 밖 정본
# (Runtime 설정 / 공개 launcher template)을 본다. 각 계약마다 자기 격리 fixture 를 만든다.
_STARTABILITY_TEST=ROOT/'cpf-tools/verification/tests/test_cpf_runtime_standalone_startability_contract.py'
_IDEMPOTENCY_TEST=ROOT/'cpf-tools/verification/tests/test_cpf_public_gradle_workspace_idempotency_contract.py'
_STARTABILITY_FILES=[
    'cpf-tools/runtime/cpf-runtime-target-catalog.json',
    'cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/api/CpfDatabaseRole.java',
    'cpf-starters/platform-operations/observability/src/main/resources/log/cpf-logback-spring.xml',
    'cpf-admin/src/main/java/com/cpf/admin/config/AdmJdbcConfig.java',
    'cpf-admin/src/main/resources/application.yml',
    'cpf-admin/src/main/resources/application-adm.yml',
    'cpf-starters/messaging/reliability/jdbc/src/main/java/com/cpf/messaging/reliability/jdbc/CpfBrokerReliabilityAutoConfiguration.java',
    'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java',
]
_IDEMPOTENCY_FILES=[
    'cpf-tools/release/open-git/open-git-surface-policy.json',
    'cpf-tools/release/open-git/templates/gradlew',
    'cpf-tools/release/open-git/templates/gradlew.bat',
    'cpf-tools/release/open-git/templates/settings.gradle',
]

def _copy_fixture(prefix, files):
    root=Path(tempfile.mkdtemp(prefix=prefix))
    for rel in files:
        dst=root/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(ROOT/rel,dst)
    return root

def _run_contract(test_path, root, env_key):
    env=dict(os.environ)
    env[env_key]=str(root); env['PYTHONUTF8']='1'; env['PYTHONIOENCODING']='utf-8'
    return subprocess.run([sys.executable,'-B',str(test_path)],cwd=str(ROOT),text=True,capture_output=True,encoding='utf-8',errors='replace',env=env)

def _contract_positive(name, test_path, prefix, files, env_key):
    if not _enabled(name): return
    root=_copy_fixture(prefix, files)
    try:
        cp=_run_contract(test_path, root, env_key)
        detail='rc='+str(cp.returncode)
        if cp.returncode!=0:
            detail+=' output='+_combined_output(cp)[-1200:].replace(chr(10),' | ')
        record(name,cp.returncode==0,detail)
    finally:
        shutil.rmtree(root,ignore_errors=True)

def _contract_mut(name, test_path, prefix, files, env_key, mutate, expected_fragment):
    if not _enabled(name): return
    root=_copy_fixture(prefix, files)
    try:
        mutate(root)
        cp=_run_contract(test_path, root, env_key)
        ok=cp.returncode!=0 and expected_fragment in _combined_output(cp)
        detail='rc='+str(cp.returncode)+' expected='+expected_fragment
        if not ok:
            detail+=' output='+_combined_output(cp)[-1200:].replace(chr(10),' | ')
        record(name,ok,detail)
    finally:
        shutil.rmtree(root,ignore_errors=True)

_contract_positive('runtime_startability_contract_positive_control',_STARTABILITY_TEST,
                   'cpf-runtime-startability-negative-',_STARTABILITY_FILES,'CPF_RUNTIME_STARTABILITY_ROOT')

def mut_runtime_logging_resource_removed(root):
    (root/'cpf-starters/platform-operations/observability/src/main/resources/log/cpf-logback-spring.xml').unlink()
_contract_mut('mutation_runtime_logging_resource_removed',_STARTABILITY_TEST,
              'cpf-runtime-startability-negative-',_STARTABILITY_FILES,'CPF_RUNTIME_STARTABILITY_ROOT',
              mut_runtime_logging_resource_removed,'존재하지 않는 logging classpath 자원')

def mut_runtime_database_role_declaration_removed(root):
    p=root/'cpf-admin/src/main/resources/application-adm.yml'
    kept=[line for line in p.read_text(encoding='utf-8').splitlines(True) if 'cpf-platform-db:' not in line]
    p.write_text(''.join(kept),encoding='utf-8')
_contract_mut('mutation_runtime_database_role_declaration_removed',_STARTABILITY_TEST,
              'cpf-runtime-startability-negative-',_STARTABILITY_FILES,'CPF_RUNTIME_STARTABILITY_ROOT',
              mut_runtime_database_role_declaration_removed,'선언되지 않은 DB role')


def mut_table_lookup_identifier_case_removed(root):
    # 저장 규칙 판정을 통째로 없애고 고정 대문자 이름만 조회하도록 되돌린다.
    p=root/'cpf-starters/messaging/reliability/jdbc/src/main/java/com/cpf/messaging/reliability/jdbc/CpfBrokerReliabilityAutoConfiguration.java'
    text=p.read_text(encoding='utf-8')
    start=text.index('String pattern = metaData.storesLowerCaseIdentifiers()')
    end=text.index(';', text.index(': table', start))+1
    text=text[:start]+'String pattern = table;'+text[end:]
    p.write_text(text,encoding='utf-8')
_contract_mut('mutation_table_lookup_identifier_case_removed',_STARTABILITY_TEST,
              'cpf-runtime-startability-negative-',_STARTABILITY_FILES,'CPF_RUNTIME_STARTABILITY_ROOT',
              mut_table_lookup_identifier_case_removed,'식별자 대소문자 규칙을 고려하지 않는')

_contract_positive('public_gradle_idempotency_contract_positive_control',_IDEMPOTENCY_TEST,
                   'cpf-public-gradle-idempotency-negative-',_IDEMPOTENCY_FILES,'CPF_PUBLIC_WORKSPACE_ROOT')

def mut_public_launcher_shared_project_cache(root):
    p=root/'cpf-tools/release/open-git/templates/gradlew'
    raw=p.read_bytes().decode('utf-8',errors='surrogateescape')
    p.write_bytes((raw+'exec gradle --project-cache-dir "$CPF_GRADLE_PROJECT_CACHE"'+chr(10)).encode('utf-8',errors='surrogateescape'))
_contract_mut('mutation_public_launcher_shared_project_cache_reentry',_IDEMPOTENCY_TEST,
              'cpf-public-gradle-idempotency-negative-',_IDEMPOTENCY_FILES,'CPF_PUBLIC_WORKSPACE_ROOT',
              mut_public_launcher_shared_project_cache,'project cache 를 강제한다')

def mut_public_root_settings_project_cache(root):
    p=root/'cpf-tools/release/open-git/templates/settings.gradle'
    p.write_text(p.read_text(encoding='utf-8')+chr(10)+"gradle.startParameter.projectCacheDir = new File(rootDir, 'shared-cache')"+chr(10),encoding='utf-8')
_contract_mut('mutation_public_root_settings_project_cache_reentry',_IDEMPOTENCY_TEST,
              'cpf-public-gradle-idempotency-negative-',_IDEMPOTENCY_FILES,'CPF_PUBLIC_WORKSPACE_ROOT',
              mut_public_root_settings_project_cache,'project cache 를 다시 덮어쓴다')


# Fresh Consumer Runtime Gate 의 개별 Finding 계약. 44건을 하나로 축약하지 않으므로 mutation 도
# Finding 하나에 하나씩 둔다. 하나의 mutation 이 여러 계약을 동시에 깨면 무엇이 지켜졌는지 알 수 없다.
_CONSUMER_TEST=ROOT/'cpf-tools/verification/tests/test_cpf_public_consumer_regression_contract.py'
_CONSUMER_FILES=[
    'cpf-docs/governance/development-harness/current/CONSUMER_RUNTIME_FINDING_LEDGER.csv',
    'cpf-tools/release/open-git/open-git-surface-policy.json',
    'cpf-tools/runtime/cli/java/CpfCli.java',
    'cpf-tools/runtime/bootstrap/CpfBootstrap.java',
    'cpf-starters/security/src/main/java/com/cpf/security/common/CpfSecurityCommonAutoConfiguration.java',
    'cpf-starters/security/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports',
    'cpf-starters/data/persistence/jdbc/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports',
    'cpf-admin/src/main/resources/application-adm.yml',
    'cpf-admin/src/main/resources/application-adm-local.yml',
    'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBootstrapInitializer.java',
    'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java',
    'cpf-backoffice/online/src/main/resources/application.yml',
    'cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/config/BackofficeSecurityConfiguration.java',
    'cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeInitialOperatorBootstrapService.java',
    'cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeInitialOperatorBootstrapRunner.java',
    'cpf-backoffice-web/src/main/java/com/cpf/backoffice/web/shared/web/BackofficeWebJsonConfiguration.java',
    'cpf-tools/db/canonical/platform-schema.json',
    'cpf-tools/db/generated/current/postgresql/cpf-platform-schema.sql',
    'cpf-tools/db/generated/current/postgresql/cpf-platform-seed.sql',
    'cpf-tools/db/generated/current/postgresql/backoffice-schema.sql',
    'cpf-tools/db/generated/current/postgresql/backoffice-seed.sql',
]
for _vendor in ('mariadb','oracle','postgresql'):
    _CONSUMER_FILES.append('cpf-tools/db/vendor/'+_vendor+'/runtime/backoffice/repository/auth-bootstrap-operator-count.sql')
    _CONSUMER_FILES.append('cpf-tools/db/vendor/'+_vendor+'/runtime/backoffice/repository/auth-bootstrap-operator.sql')
_CONSUMER_PREFIX='cpf-consumer-regression-negative-'
_CONSUMER_ENV='CPF_CONSUMER_REGRESSION_ROOT'

def _consumer_mut(name, mutate, expected):
    _contract_mut(name,_CONSUMER_TEST,_CONSUMER_PREFIX,_CONSUMER_FILES,_CONSUMER_ENV,mutate,expected)

def _sub(root, rel, old, new):
    p=root/rel; t=p.read_text(encoding='utf-8')
    if old not in t: raise AssertionError('mutation anchor missing in '+rel)
    p.write_text(t.replace(old,new),encoding='utf-8')

def _drop_lines(root, rel, needle):
    p=root/rel
    p.write_text(''.join(l for l in p.read_text(encoding='utf-8').splitlines(True) if needle not in l),encoding='utf-8')

_contract_positive('consumer_regression_contract_positive_control',_CONSUMER_TEST,
                   _CONSUMER_PREFIX,_CONSUMER_FILES,_CONSUMER_ENV)

# 원장 자체가 Finding 단위를 잃는 되돌림.
def mut_finding_ledger_collapsed(root):
    import csv as _csv
    p=root/'cpf-docs/governance/development-harness/current/CONSUMER_RUNTIME_FINDING_LEDGER.csv'
    with p.open(encoding='utf-8-sig',newline='') as f:
        rd=_csv.DictReader(f); cols=rd.fieldnames; rows=list(rd)
    for r in rows: r['direct_root_cause']=r['upper_root_cause']
    with p.open('w',encoding='utf-8',newline='') as f:
        w=_csv.DictWriter(f,fieldnames=cols,lineterminator=chr(10)); w.writeheader(); w.writerows(rows)
_consumer_mut('mutation_finding_ledger_collapsed_into_one',mut_finding_ledger_collapsed,
              '직접 Root Cause 가 중복된 Finding')

# CRF-11 공개 Profile 의 runtime lifecycle 을 내부 엔진 위임으로 되돌린다.
# Lifecycle selector 를 profile 별로 다른 engine 에 위임하도록 되돌린다.
_consumer_mut('mutation_public_runtime_lifecycle_delegates_internal_engine',
              lambda root: _sub(root,'cpf-tools/runtime/cli/java/CpfCli.java',
                                'return requireJava25Then(() -> runClass(root, "CpfBootstrap", forwarded));',
                                'return requireJava25Then(() -> internalRuntime(root, action, forwarded));'),
              'Lifecycle selector가 canonical Java engine으로 가지 않는다')

# CRF-14 ADM 이 요구하는 CmnCryptoService 공급을 없앤다.
_consumer_mut('mutation_security_common_crypto_service_unregistered',
              lambda root: _sub(root,'cpf-starters/security/src/main/java/com/cpf/security/common/CpfSecurityCommonAutoConfiguration.java',
                                'CmnCryptoService cmnCryptoService(','CmnCryptoService disabledCryptoService('),
              'CmnCryptoService 를 공급하지 않는다')

# CRF-17 Runtime 환경에서 DB vendor 전달을 없앤다.
_consumer_mut('mutation_runtime_environment_db_vendor_removed',
              lambda root: _sub(root,'cpf-tools/runtime/bootstrap/CpfBootstrap.java',
                                'baseEnv.put("CPF_DB_VENDOR"','baseEnv.remove("CPF_DB_VENDOR_DISABLED"'),
              'Runtime 환경에 DB vendor 를 전달하지 않는다')

# CRF-18 Vendor SQL Pack 공개 투영을 없앤다.
def mut_public_vendor_pack_projection_removed(root):
    import json as _json
    p=root/'cpf-tools/release/open-git/open-git-surface-policy.json'
    policy=_json.loads(p.read_text(encoding='utf-8'))
    for key in ('templateRules','sourceRules'):
        policy[key]=[r for r in policy.get(key,[]) if 'deploy/local/db/vendor/' not in str(r.get('target',''))]
    p.write_text(_json.dumps(policy,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')
_consumer_mut('mutation_public_vendor_pack_projection_removed',mut_public_vendor_pack_projection_removed,
              'Vendor SQL Pack 이 공개 배포본에 투영되지 않는다')

# CRF-19 local 전용 Runtime Secret 준비를 없앤다.
_consumer_mut('mutation_bootstrap_local_secret_generation_removed',
              lambda root: _sub(root,'cpf-tools/runtime/bootstrap/CpfBootstrap.java',
                                'ensureLocalSecret(','skipLocalSecret('),
              'local 전용 Runtime Secret 준비 경로가 없다')

# CRF-20 Platform role 기본 선택 AutoConfiguration 등록을 없앤다.
_consumer_mut('mutation_platform_role_primary_autoconfiguration_unregistered',
              lambda root: _drop_lines(root,'cpf-starters/data/persistence/jdbc/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports',
                                       'CpfPlatformRoleDataSourcePrimaryAutoConfiguration'),
              'Platform role 기본 선택이 Starter AutoConfiguration 으로 제공되지 않는다')

# CRF-21 다중 생성자 Bean 의 주입 생성자 표시를 없앤다.
_consumer_mut('mutation_injection_constructor_annotation_removed',
              lambda root: _drop_lines(root,'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java',
                                       'Autowired'),
              '다중 생성자 Bean 이 주입 생성자를 명시하지 않는다')

# CRF-22 SystemCode 없는 Platform Runtime 의 Service Identity 선언을 없앤다.
_consumer_mut('mutation_platform_runtime_service_identity_removed',
              lambda root: _drop_lines(root,'cpf-admin/src/main/resources/application-adm.yml','service-id:'),
              'Service Identity 를 명시하지 않는다')

# CRF-24 ADM ApplicationRunner 의 관리 실행 Context 를 없앤다.
_consumer_mut('mutation_adm_bootstrap_runner_context_scope_removed',
              lambda root: _sub(root,'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBootstrapInitializer.java',
                                'CpfContexts.bind(','CpfContextsDisabled.noBind('),
              'AdmBootstrapInitializer.java')

# CRF-25 관리 Surface 의 업무 Header 강제 제외 선언을 없앤다.
_consumer_mut('mutation_control_plane_management_root_paths_removed',
              lambda root: _drop_lines(root,'cpf-admin/src/main/resources/application-adm.yml','management-root-paths:'),
              '업무 Header 강제 대상에서 제외되지 않는다')

# CRF-27 SPA 진입 경로만 목록에서 뺀다. (CRF-25 와 다른 되돌림이다)
_consumer_mut('mutation_control_plane_adm_root_path_unlisted',
              lambda root: _drop_lines(root,'cpf-admin/src/main/resources/application-adm.yml','- /adm'),
              '업무 Header 강제 제외 목록에 없다')

# CRF-28 local 평문 HTTP 경로에서 Secure 쿠키를 되돌린다.
_consumer_mut('mutation_local_profile_secure_cookie_reentry',
              lambda root: _sub(root,'cpf-admin/src/main/resources/application-adm-local.yml',
                                'CPF_ADM_SESSION_COOKIE_SECURE:false','CPF_ADM_SESSION_COOKIE_SECURE:true'),
              '세션/CSRF 쿠키가 성립하지 않는다')

# CRF-30 업무 Domain Runtime 의 Platform DB role 선언을 없앤다.
_consumer_mut('mutation_domain_runtime_platform_db_role_removed',
              lambda root: _drop_lines(root,'cpf-backoffice/online/src/main/resources/application.yml','cpf-platform-db:'),
              '업무 Domain Runtime 이 자기 설정으로 CPF_PLATFORM_DB role 을 해석하지 못한다')

# CRF-31 Domain DataSource prefix 선언을 없앤다.
_consumer_mut('mutation_domain_persistence_declaration_removed',
              lambda root: _drop_lines(root,'cpf-backoffice/online/src/main/resources/application.yml','data-source-prefix:'),
              'Domain DataSource 를 만들 prefix 선언이 없다')

# CRF-32 MBW 가 요구하는 CmnJwtService 공급을 없앤다.
_consumer_mut('mutation_security_common_jwt_service_unregistered',
              lambda root: _sub(root,'cpf-starters/security/src/main/java/com/cpf/security/common/CpfSecurityCommonAutoConfiguration.java',
                                'CmnJwtService cmnJwtService(','CmnJwtService disabledJwtService('),
              'CmnJwtService 를 공급하지 않는다')

# CRF-33 프록시 대상 Bean 을 final 로 되돌린다.
_consumer_mut('mutation_transactional_bean_made_final',
              lambda root: _sub(root,'cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeInitialOperatorBootstrapService.java',
                                'public class BackofficeInitialOperatorBootstrapService',
                                'public final class BackofficeInitialOperatorBootstrapService'),
              '프록시 대상 Bean 이 final class 다')

# CRF-34 @CpfService 의 Domain Base Class 상속을 없앤다.
def mut_cpf_service_base_class_removed(root):
    rel='cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeInitialOperatorBootstrapService.java'
    p=root/rel; t=p.read_text(encoding='utf-8')
    head='class BackofficeInitialOperatorBootstrapService'
    i=t.index(head)+len(head); j=t.index('{',i)
    p.write_text(t[:i]+' '+t[j:],encoding='utf-8')
_consumer_mut('mutation_cpf_service_base_class_removed',mut_cpf_service_base_class_removed,
              'Domain Base Class 를 상속하지 않는다')

# CRF-35 구현과 다른 persistence provider 를 선언한다.
_consumer_mut('mutation_domain_persistence_provider_mismatch',
              lambda root: _sub(root,'cpf-backoffice/online/src/main/resources/application.yml',
                                'provider: jdbc','provider: mybatis'),
              'mybatis provider 를 선언했는데 mapper 자원이 없다')

# CRF-36 Target 별 instanceId 부여를 없앤다.
_consumer_mut('mutation_runtime_instance_id_assignment_removed',
              lambda root: _sub(root,'cpf-tools/runtime/bootstrap/CpfBootstrap.java',
                                'runtimeHostName() + "-" + target.name()','runtimeHostName()'),
              'instanceId 가 Target 별로 유일하지 않다')

# CRF-37 정본 제약 밖의 Runtime Role 값으로 되돌린다.
_consumer_mut('mutation_runtime_role_value_out_of_contract',
              lambda root: _sub(root,'cpf-backoffice/online/src/main/resources/application.yml',
                                'role: APPLICATION','role: ONLINE'),
              'Runtime Role 이 정본 제약 밖의 값이다')

# CRF-38 Prebuilt Domain DDL 공개 투영을 없앤다.
def mut_prebuilt_domain_ddl_projection_removed(root):
    import json as _json
    p=root/'cpf-tools/release/open-git/open-git-surface-policy.json'
    policy=_json.loads(p.read_text(encoding='utf-8'))
    for key in ('templateRules','sourceRules'):
        policy[key]=[r for r in policy.get(key,[]) if 'deploy/local/db/domain/' not in str(r.get('target',''))]
    p.write_text(_json.dumps(policy,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')
_consumer_mut('mutation_prebuilt_domain_ddl_projection_removed',mut_prebuilt_domain_ddl_projection_removed,
              'Prebuilt Domain DDL 이 공개 자산이 아니다')

# CRF-39 schema 가 선언하지 않은 유일키로 seed 가 충돌 처리하도록 되돌린다.
def mut_canonical_seed_conflict_target_invalid(root):
    p=root/'cpf-tools/db/generated/current/postgresql/backoffice-seed.sql'
    t=p.read_text(encoding='utf-8')
    i=t.index('ON CONFLICT ('); j=t.index(')',i)
    p.write_text(t[:i]+'ON CONFLICT (created_at'+t[j:],encoding='utf-8')
_consumer_mut('mutation_canonical_seed_conflict_target_invalid',mut_canonical_seed_conflict_target_invalid,
              'schema 가 선언하지 않은 유일키로 seed 가 충돌 처리한다')

# CRF-40 Channel Front 의 Jackson 2 Mapper 공급을 없앤다.
_consumer_mut('mutation_channel_front_object_mapper_removed',
              lambda root: _sub(root,'cpf-backoffice-web/src/main/java/com/cpf/backoffice/web/shared/web/BackofficeWebJsonConfiguration.java',
                                'com.fasterxml.jackson.databind.ObjectMapper','tools.jackson.databind.ObjectMapper'),
              'Jackson 2 Mapper 를 공급하지 않는다')

# CRF-41 설치 완료 판정이 인증 불가능한 행까지 세도록 되돌린다.
def mut_initial_operator_predicate_counts_credentialless(root):
    for vendor in ('mariadb','oracle','postgresql'):
        _sub(root,'cpf-tools/db/vendor/'+vendor+'/runtime/backoffice/repository/auth-bootstrap-operator-count.sql',
             'password_hash IS NOT NULL','1 = 1')
_consumer_mut('mutation_initial_operator_predicate_counts_credentialless',
              mut_initial_operator_predicate_counts_credentialless,
              '설치 완료 판정이 인증 불가능한 행까지 센다')

# CRF-46 최초 운영자를 강제 비밀번호 변경 상태로 되돌린다.
def mut_initial_operator_forced_password_change(root):
    for vendor in ('mariadb','oracle','postgresql'):
        _sub(root,'cpf-tools/db/vendor/'+vendor+'/runtime/backoffice/repository/auth-bootstrap-operator.sql',
             "0, 'N', :passwordExpireAt","0, 'Y', :passwordExpireAt")
_consumer_mut('mutation_initial_operator_forced_password_change',mut_initial_operator_forced_password_change,
              '최초 운영자가 강제 비밀번호 변경 상태로 생성된다')

# CRF-42 cpf runtime start 를 bootRun 위임으로 되돌린다.
def mut_source_runtime_uses_bootrun(root):
    rel='cpf-tools/runtime/bootstrap/CpfBootstrap.java'
    _sub(root,rel,': buildSourceRuntimeJar(target);',': null;')
    _sub(root,rel,'return List.of(javaExecutable(), "-jar", jar.toString());',
         'return List.of("gradlew", "bootRunNothing");')
_consumer_mut('mutation_source_runtime_uses_bootrun',mut_source_runtime_uses_bootrun,
              'runtimeCommand 가 실행물을 직접 띄우지 않는다')

# CRF-43 MBW ApplicationRunner 의 관리 실행 Context 를 없앤다.
_consumer_mut('mutation_mbw_bootstrap_runner_context_scope_removed',
              lambda root: _sub(root,'cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/auth/service/BackofficeInitialOperatorBootstrapRunner.java',
                                'CpfContexts.bind(','CpfContextsDisabled.noBind('),
              'BackofficeInitialOperatorBootstrapRunner.java')

# CRF-44 Runtime 의 자기 인가 경계 선언을 없앤다.
_consumer_mut('mutation_runtime_security_chain_removed',
              lambda root: _sub(root,'cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/config/BackofficeSecurityConfiguration.java',
                                'SecurityFilterChain','DisabledFilterChain'),
              '자기 인가 경계를 선언하지 않는다')

# CRF-45 cpf bootstrap --run 을 bootRun 위임으로 되돌린다.
def mut_domain_runtime_started_by_bootrun(root):
    rel='cpf-tools/runtime/bootstrap/CpfBootstrap.java'
    _sub(root,rel,'Path runnable = buildDomainRuntimeJar(gradlew, d);'+chr(10),'')
    _sub(root,rel,'new ProcessBuilder(javaExecutable(), "-jar", runnable.toString())',
         'new ProcessBuilder(gradlew.toString(), "-p", d.project.toString(), ":online:bootRun", "--no-daemon")')
_consumer_mut('mutation_domain_runtime_started_by_bootrun',mut_domain_runtime_started_by_bootrun,
              'bootstrap 이 Gradle wrapper 를 Runtime pid 로 기록한다')

# 식별자 namespace 혼동(SystemCode != Service Identity != Runtime Role)의 되돌림.
def mut_runtime_role_namespaces_merged(root):
    import json as _json
    p=root/'cpf-tools/db/canonical/platform-schema.json'
    model=_json.loads(p.read_text(encoding='utf-8'))
    for table in model.get('tables',[]):
        for check in (table.get('checkConstraints') or table.get('checks') or []):
            if check.get('name')=='ck_bat_runtime_instance_role':
                for key in list(check.keys()):
                    if key=='expression':
                        check[key]=str(check[key]).replace('IN (',"IN ('APPLICATION',")
                    elif isinstance(check[key],dict):
                        for vk,vv in list(check[key].items()):
                            check[key][vk]=str(vv).replace('IN (',"IN ('APPLICATION',")
    p.write_text(_json.dumps(model,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')
_consumer_mut('mutation_runtime_role_namespaces_merged',mut_runtime_role_namespaces_merged,
              'Batch Platform 전용 role 열거가 Platform Runtime role 과 합쳐졌다')

# CRF-26 생성 키 컬럼 명시를 없앤다. 계약 정본은 단독 기동 계약 쪽이다.
def mut_generated_key_columns_removed(root):
    p=root/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java'
    t=p.read_text(encoding='utf-8')
    i=t.index('new String[]{"delivery_id"}')
    p.write_text(t[:i]+'java.sql.Statement.RETURN_GENERATED_KEYS'+t[i+len('new String[]{"delivery_id"}'):],encoding='utf-8')
_contract_mut('mutation_generated_key_columns_removed',_STARTABILITY_TEST,
              'cpf-runtime-startability-negative-',_STARTABILITY_FILES,'CPF_RUNTIME_STARTABILITY_ROOT',
              mut_generated_key_columns_removed,'생성 키 컬럼을 명시하지 않은 INSERT')

# Runtime Lifecycle CLI 계약. 사용자가 내부 구조를 몰라도 canonical `cpf` 하나로 운영할 수 있어야
# 한다. 계약 하나에 mutation 하나를 둔다. 하나의 mutation 이 여러 계약을 깨면 무엇이 지켜졌는지 모른다.
_CLI_TEST=ROOT/'cpf-tools/verification/tests/test_cpf_runtime_lifecycle_cli_contract.py'
_CLI_FILES=[
    'cpf-tools/runtime/cpf-runtime-target-catalog.json',
    'cpf-tools/runtime/cli/java/CpfCli.java',
    'cpf-tools/runtime/cli/java/CpfRuntimeTargets.java',
    'cpf-tools/runtime/cli/java/CpfGeneratorLauncher.java',
    'cpf-tools/runtime/bootstrap/CpfBootstrap.java',
    'cpf-tools/release/open-git/templates/README.md',
    'cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md',
    'cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv',
    'cpf-tools/release/open-git/templates/build.gradle',
    'cpf-tools/release/open-git/open-git-surface-policy.json',
    'cpf-tools/release/open-git/cpf_open_git.py',
]
for _script in sorted((ROOT/'cpf-tools/release/open-git/templates/bin').glob('cpf*')):
    _CLI_FILES.append('cpf-tools/release/open-git/templates/bin/'+_script.name)
_CLI_PREFIX='cpf-runtime-cli-negative-'
_CLI_ENV='CPF_RUNTIME_CLI_ROOT'
_CLI_CATALOG='cpf-tools/runtime/cpf-runtime-target-catalog.json'

def _cli_mut(name, mutate, expected):
    _contract_mut(name,_CLI_TEST,_CLI_PREFIX,_CLI_FILES,_CLI_ENV,mutate,expected)

def _catalog(root):
    import json as _json
    return _json.loads((root/_CLI_CATALOG).read_text(encoding='utf-8'))

def _write_catalog(root, model):
    import json as _json
    (root/_CLI_CATALOG).write_text(_json.dumps(model,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')

def _cli_sub(root, rel, old, new):
    p=root/rel; t=p.read_text(encoding='utf-8')
    if old not in t: raise AssertionError('mutation anchor missing in '+rel)
    p.write_text(t.replace(old,new,1),encoding='utf-8')

_contract_positive('runtime_lifecycle_cli_contract_positive_control',_CLI_TEST,
                   _CLI_PREFIX,_CLI_FILES,_CLI_ENV)

def mut_group_authority_removed(root):
    model=_catalog(root)
    model['runtimeGroups']['groups']=[g for g in model['runtimeGroups']['groups'] if g['group']!='batch']
    _write_catalog(root,model)
_cli_mut('mutation_runtime_group_authority_removed',mut_group_authority_removed,
         'canonical Runtime Group 이 없다')

def mut_group_membership_hardcoded(root):
    # Group 을 대상 이름 목록으로 정의한다. 새 Runtime 이 생겨도 Group 에 들어오지 않는다.
    model=_catalog(root)
    for group in model['runtimeGroups']['groups']:
        if group['group']=='platform':
            group['selector']='members'; group['members']=['admin','gateway']
    _write_catalog(root,model)
_cli_mut('mutation_runtime_group_membership_hardcoded',mut_group_membership_hardcoded,
         'metadata selector 가 아닌 방식으로 정의됐다')

def mut_group_name_collision(root):
    model=_catalog(root)
    for group in model['runtimeGroups']['groups']:
        if group['group']=='backoffice-stack': group['group']='backoffice'
    _write_catalog(root,model)
_cli_mut('mutation_group_name_collides_with_target_name',mut_group_name_collision,
         'Group 이름과 Runtime target 이름이 겹친다')

def mut_domain_target_module_qualified(root):
    model=_catalog(root)
    model['dynamicRuntimes']['targetPattern']='{domainName}-{module}'
    _write_catalog(root,model)
_cli_mut('mutation_generated_domain_target_name_module_qualified',mut_domain_target_module_qualified,
         '사용자가 module 이름까지 알아야 Domain 을 띄울 수 있다')

def mut_domain_name_hardcoded(root):
    # 실재하는 Domain 이름으로 분기를 넣는다. 새 Domain 마다 CLI 를 고쳐야 하는 구조다.
    import re as _re
    names=[]
    for properties in (root).glob('cpf-*/gradle.properties'):
        text=properties.read_text(encoding='utf-8')
        m=_re.search(r'^cpf\\.domain\\.name\\s*=\\s*(\\S+)',text,_re.M)
        if m: names.append(m.group(1))
    if not names: names=['backoffice']
    _cli_sub(root,'cpf-tools/runtime/cli/java/CpfCli.java',
             'String requested = target.trim();',
             'String requested = target.trim();'+chr(10)
             +'            if (requested.equals("'+names[0]+'")) requested = requested.trim();')
_cli_mut('mutation_generated_domain_name_hardcoded_in_cli',mut_domain_name_hardcoded,
         'CLI Source 가 Target/Group 이름으로 분기한다')

def mut_target_list_duplicated(root):
    _cli_sub(root,'cpf-tools/runtime/cli/java/CpfCli.java',
             'String requested = target.trim();',
             'List<String> known = List.of("admin", "gateway");'+chr(10)
             +'            if (known.isEmpty()) return EXIT_OK;'+chr(10)
             +'            String requested = target.trim();')
_cli_mut('mutation_runtime_target_list_duplicated_in_cli',mut_target_list_duplicated,
         'CLI Source 가 Target/Group 목록을 들고 있다')

def mut_new_batch_runtime_orphaned(root):
    # batch Group 을 architectureRole 이 아니라 특정 tag 로 좁힌다. 신규 Batch Runtime 이 빠진다.
    model=_catalog(root)
    for group in model['runtimeGroups']['groups']:
        if group['group']=='batch':
            group['selector']='runtimeGroups'; group['value']='legacy-batch-only'
    _write_catalog(root,model)
_cli_mut('mutation_new_batch_runtime_missing_from_batch_group',mut_new_batch_runtime_orphaned,
         '대상이 없는 Group')

def mut_all_group_narrowed(root):
    model=_catalog(root)
    for group in model['runtimeGroups']['groups']:
        if group['group']=='all':
            group['selector']='architectureRole'; group['value']='CONTROL_PLANE_RUNTIME'
    _write_catalog(root,model)
_cli_mut('mutation_all_group_narrowed_to_a_subset',mut_all_group_narrowed,
         'all Group 이 전체 Target 을 선택하지 않는다')

def mut_dependency_cycle(root):
    model=_catalog(root)
    for entry in model['runtimes']:
        if entry['target']=='backoffice': entry['dependsOn']=['backoffice-web']
    _write_catalog(root,model)
_cli_mut('mutation_runtime_dependency_cycle_introduced',mut_dependency_cycle,
         'Runtime dependency cycle')

def mut_channel_front_dependency_removed(root):
    model=_catalog(root)
    for entry in model['runtimes']:
        if entry['architectureRole']=='CHANNEL_FRONT_RUNTIME': entry['dependsOn']=[]
    _write_catalog(root,model)
_cli_mut('mutation_channel_front_dependency_removed',mut_channel_front_dependency_removed,
         '업무 Runtime 기동 순서를 선언하지 않는다')

def mut_partial_failure_hidden(root):
    _cli_sub(root,'cpf-tools/runtime/bootstrap/CpfBootstrap.java',
             'if (outcome.equals("FAIL")) failed = true;','')
_cli_mut('mutation_group_partial_failure_reported_as_pass',mut_partial_failure_hidden,
         '일부 실패가 전체 결과에 반영되지 않는다')

def mut_status_healthy_from_pid(root):
    p=root/'cpf-tools/runtime/bootstrap/CpfBootstrap.java'
    t=p.read_text(encoding='utf-8')
    head,rest=t.split('private int runtimeGroupStatus(',1)
    body,tail=rest.split(chr(10)+'    }',1)
    body=body.replace('probeHealth(','pidOnlyHealth(')
    p.write_text(head+'private int runtimeGroupStatus('+body+chr(10)+'    }'+tail,encoding='utf-8')
_cli_mut('mutation_group_status_healthy_from_pid_alone',mut_status_healthy_from_pid,
         'pid 존재만으로 상태를 판정한다')

def mut_stop_keeps_start_order(root):
    _cli_sub(root,'cpf-tools/runtime/bootstrap/CpfBootstrap.java',
             'if (action.equals("stop")) java.util.Collections.reverse(ordered);','')
_cli_mut('mutation_stop_keeps_start_order',mut_stop_keeps_start_order,
         '정지가 기동 순서의 역순이 아니다')

def mut_unsupported_silently_passed(root):
    _cli_sub(root,'cpf-tools/runtime/bootstrap/CpfBootstrap.java',
             'outcome = "UNSUPPORTED";','outcome = "PASS";')
_cli_mut('mutation_unsupported_capability_silently_passed',mut_unsupported_silently_passed,
         'UNSUPPORTED 로 알리지 않는다')

def mut_launcher_verb_parity(root):
    _cli_sub(root,'cpf-tools/release/open-git/templates/bin/cpf-start.sh',
             '"$ROOT/cpf" start','"$ROOT/cpf" run')
_cli_mut('mutation_launcher_verb_parity_broken',mut_launcher_verb_parity,
         'Windows/Linux wrapper 의미가 다르다')

def mut_powershell_target_translation(root):
    _cli_sub(root,'cpf-tools/release/open-git/templates/bin/cpf-start.ps1',
             "@('--target', $Target)","@($Target)")
_cli_mut('mutation_powershell_wrapper_target_translation_removed',mut_powershell_target_translation,
         '-Target 을 --target 으로 넘기지 않는다')

def mut_launcher_reimplements_parsing(root):
    p=root/'cpf-tools/release/open-git/templates/bin/cpf.sh'
    p.write_text(p.read_text(encoding='utf-8')
                 +'case "${1:-}" in start) echo start ;; *) echo other ;; esac'+chr(10),encoding='utf-8')
_cli_mut('mutation_launcher_reimplements_command_parsing',mut_launcher_reimplements_parsing,
         'wrapper 가 자체 명령 목록을 갖고 있다')

def mut_readme_command_not_in_cli(root):
    p=root/'cpf-tools/release/open-git/templates/README.md'
    p.write_text(p.read_text(encoding='utf-8')
                 +chr(10)+'```bash'+chr(10)+'./bin/cpf launch all'+chr(10)+'```'+chr(10),encoding='utf-8')
_cli_mut('mutation_readme_command_not_in_cli',mut_readme_command_not_in_cli,
         'README 가 CLI 에 없는 명령을 안내한다')

def mut_public_runtime_undocumented(root):
    # 배포되는 Runtime 하나의 실행 안내를 README 에서 지운다.
    p=root/'cpf-tools/release/open-git/templates/README.md'
    text=p.read_text(encoding='utf-8')
    kept=[l for l in text.splitlines(True) if 'cpf start education' not in l and 'EDU_SERVER_PORT' not in l]
    p.write_text(''.join(kept),encoding='utf-8')
_cli_mut('mutation_public_runtime_left_undocumented',mut_public_runtime_undocumented,
         'README 가 배포되는 Runtime 의 실행 방법을 알려주지 않는다')

def mut_public_runtime_not_obtainable(root):
    # binary 공개 Runtime 의 발행 좌표를 지운다. 공개로 표시했는데 실행물을 얻을 길이 없다.
    import json as _json
    p=root/_CLI_CATALOG
    model=_json.loads(p.read_text(encoding='utf-8'))
    for entry in model['runtimes']:
        if entry.get('provision')=='binary' and entry.get('publicationClass')=='PUBLIC_RUNTIME':
            entry['artifactId']=''
    p.write_text(_json.dumps(model,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')
_cli_mut('mutation_public_runtime_not_obtainable',mut_public_runtime_not_obtainable,
         '공개 Runtime 인데 배포본에서 얻을 수 없다')

def mut_lifecycle_verb_not_public(root):
    _cli_sub(root,'cpf-tools/runtime/cli/java/CpfCli.java',
             '"targets", "start", "stop", "restart"','"targets", "stop", "restart"')
_cli_mut('mutation_lifecycle_verb_not_public',mut_lifecycle_verb_not_public,
         '공개 명령이 아닌 Lifecycle 동사')

def mut_harness_rule_removed(root):
    p=root/'cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md'
    text=p.read_text(encoding='utf-8')
    p.write_text(text.split('## 38. Runtime Lifecycle CLI')[0],encoding='utf-8')
_cli_mut('mutation_harness_lifecycle_cli_rule_removed',mut_harness_rule_removed,
         'Current Harness 에 Runtime Lifecycle CLI 계약이 없다')

def mut_registry_relation_removed(root):
    p=root/'cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv'
    p.write_text(p.read_text(encoding='utf-8').replace('test_cpf_runtime_lifecycle_cli_contract','REMOVED'),
                 encoding='utf-8')
_cli_mut('mutation_registry_validator_relation_removed',mut_registry_relation_removed,
         'Registry 가 이 계약 Validator 를 참조하지 않는다')

# 내부 개발 환경도 하나의 project cache 를 공유해야 한다(Harness 36.2 (4)).
_SHELL_TEST=ROOT/'cpf-tools/verification/tests/test_cpf_developer_shell_contract.py'
_SHELL_FILES=['.vscode/settings.json','cpf-tools/build/cpf-root-conventions.gradle']

def mut_ide_separate_project_cache(root):
    import json as _json
    p=root/'.vscode/settings.json'
    settings=_json.loads(p.read_text(encoding='utf-8'))
    settings['java.import.gradle.arguments']=(
        '--project-cache-dir '
        'cpf-docs/governance/development-harness/evidence/platform/current/generated/gradle/project-cache')
    p.write_text(_json.dumps(settings,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')
_contract_mut('mutation_ide_separate_project_cache_reentry',_SHELL_TEST,
              'cpf-ide-project-cache-negative-',_SHELL_FILES,'CPF_DEVELOPER_SHELL_ROOT',
              mut_ide_separate_project_cache,'IDE import 가 CLI 와 다른 project cache 를 쓰면')

# 파괴적 재생성은 모든 전제조건 확인 뒤에만 수행한다(CRF-49).
_OPEN_GIT_TASK_TEST=ROOT/'cpf-tools/verification/tests/test_cpf_open_git_task_contract.py'
_OPEN_GIT_TASK_FILES=[
    'cpf-tools/release/open-git/cpf_open_git.py',
    'cpf-tools/release/open-git/cpf-open-git.ps1',
    'cpf-tools/build/cpf-root-conventions.gradle',
]

def mut_release_cleanup_before_prerequisite(root):
    # remote 확인을 다시 삭제 뒤로 옮긴다.
    p=root/'cpf-tools/release/open-git/cpf_open_git.py'
    t=p.read_text(encoding='utf-8')
    line='    remote = canonical_remote(root, remote_arg)'+chr(10)
    if line not in t: raise AssertionError('remote 확인 지점을 찾지 못했다')
    t=t.replace(line,'',1)
    marker='    version = platform_version(root)'+chr(10)
    if marker not in t: raise AssertionError('version 확인 지점을 찾지 못했다')
    p.write_text(t.replace(marker,marker+line,1),encoding='utf-8')
_contract_mut('mutation_release_cleanup_before_prerequisite_check',_OPEN_GIT_TASK_TEST,
              'cpf-open-git-task-negative-',_OPEN_GIT_TASK_FILES,'CPF_OPEN_GIT_TASK_ROOT',
              mut_release_cleanup_before_prerequisite,
              '확인하기 전에 직전 Release 산출물을 삭제한다')

# Release Asset 보존 / Fresh 재생성 계약(Harness 39).
# "Master 에 저장한다" 와 "다음 Release 에 재사용한다" 를 섞는 되돌림을 각각 잡는다.
_ASSET_TEST=ROOT/'cpf-tools/verification/tests/test_cpf_release_asset_freshness_contract.py'
_ASSET_FILES=[
    'cpf-tools/release/open-git/open-git-surface-policy.json',
    'cpf-tools/release/open-git/cpf_open_git.py',
    'cpf-tools/release/open-git/verify_release_lfs_contract.py',
    'cpf-tools/db/canonical/platform-schema.json',
    'cpf-tools/runtime/cpf-runtime-target-catalog.json',
    '.gitattributes',
    '.gitignore',
    'cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md',
    'cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv',
]
for _launcher in sorted((ROOT/'cpf-tools/release/open-git/templates/bin').glob('cpf*')):
    _ASSET_FILES.append('cpf-tools/release/open-git/templates/bin/'+_launcher.name)
_ASSET_FILES.append('cpf-tools/release/open-git/report_release_payload_composition.py')
_ASSET_FILES.append('cpf-tools/release/open-git/report_release_binary_tracking.py')
_ASSET_PREFIX='cpf-release-asset-negative-'
_ASSET_ENV='CPF_RELEASE_ASSET_ROOT'
_ASSET_POLICY='cpf-tools/release/open-git/open-git-surface-policy.json'
_ASSET_CANONICAL='CANONICAL_RELEASE_SOURCE'
_ASSET_TRACKED='TRACKED_VERIFIED_RELEASE_RESULT'
_ASSET_UNTRACKED='UNTRACKED_RELEASE_RESULT'
_ASSET_LARGE='LARGE_RELEASE_BINARY'

def _asset_mut(name, mutate, expected):
    _contract_mut(name,_ASSET_TEST,_ASSET_PREFIX,_ASSET_FILES,_ASSET_ENV,mutate,expected)

def _asset_model(root):
    import json as _json
    return _json.loads((root/_ASSET_POLICY).read_text(encoding='utf-8'))

def _asset_write(root, model):
    import json as _json
    (root/_ASSET_POLICY).write_text(_json.dumps(model,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')

def _asset_edit(root, mutate):
    model=_asset_model(root); mutate(model['releaseAssetPolicy']); _asset_write(root,model)

_contract_positive('release_asset_freshness_contract_positive_control',_ASSET_TEST,
                   _ASSET_PREFIX,_ASSET_FILES,_ASSET_ENV)

def mut_authority_removed(root):
    model=_asset_model(root); model.pop('releaseAssetPolicy',None); _asset_write(root,model)
_asset_mut('mutation_release_asset_authority_removed',mut_authority_removed,
           'Release Asset 분류 정본')

def mut_produced_assets_removed(root):
    model=_asset_model(root); model.pop('releaseProducedAssets',None); _asset_write(root,model)
_asset_mut('mutation_produced_asset_declaration_removed',mut_produced_assets_removed,
           'Release 엔진 생성 자산 선언이 없다')

def mut_produced_asset_as_input(root):
    model=_asset_model(root)
    for asset in model['releaseProducedAssets']['assets']:
        asset['releaseAssetClass']=_ASSET_CANONICAL
    _asset_write(root,model)
_asset_mut('mutation_produced_asset_becomes_release_input',mut_produced_asset_as_input,
           '엔진 생성 자산이 다음 Release 의 입력 권한을 갖는다')

_asset_mut('mutation_release_asset_axis_declaration_removed',
           lambda root: _asset_edit(root, lambda a: a['axes'].pop('releaseInputAuthority',None)),
           'Release Asset 축 선언이 없다')

_asset_mut('mutation_release_asset_classification_unmapped',
           lambda root: _asset_edit(root, lambda a: a['classificationMapping'].pop('OPEN_GIT_USER_SCRIPT',None)),
           'Release Asset 부류가 없는 classification')

def _map_set(key, value):
    def apply(a): a['classificationMapping'][key]=value
    return apply

_asset_mut('mutation_release_asset_classified_by_path_name',
           lambda root: _asset_edit(root,_map_set('bin/cpf',_ASSET_CANONICAL)),
           '경로로 분류한 항목이 있다')

_asset_mut('mutation_release_asset_classified_by_extension',
           lambda root: _asset_edit(root,_map_set('.jar',_ASSET_TRACKED)),
           '확장자로 분류한 항목이 있다')

# 보존과 입력을 섞는 되돌림.
def _class_set(name, key, value):
    def apply(a): a['classes'][name][key]=value
    return apply

_asset_mut('mutation_tracked_result_becomes_release_input',
           lambda root: _asset_edit(root,_class_set(_ASSET_TRACKED,'releaseInputAuthority',True)),
           '다음 Release 의 입력으로 쓴다')

_asset_mut('mutation_tracked_result_exempted_from_fresh_regeneration',
           lambda root: _asset_edit(root,_class_set(_ASSET_TRACKED,'freshRegenerationRequired',False)),
           'Fresh 재생성 의무를 면제받는다')

_asset_mut('mutation_public_surface_inferred_from_master_tracking',
           lambda root: _asset_edit(root,_class_set(_ASSET_TRACKED,'publicRelease',True)),
           '공개 여부를 Master 보존 여부로 정한다')

_asset_mut('mutation_untracked_result_forced_out_of_public_release',
           lambda root: _asset_edit(root,_class_set(_ASSET_UNTRACKED,'publicRelease',False)),
           'Master 미보존이 공개 배포 제외로 연결된다')

_asset_mut('mutation_tracking_exception_reason_removed',
           lambda root: _asset_edit(root,_class_set(_ASSET_UNTRACKED,'trackingExceptionReasonRequired',False)),
           'Master 보존 제외에 이유를 요구하지 않는다')

def mut_exception_evidence_reduced(root):
    def apply(a):
        evidence=a['classes'][_ASSET_UNTRACKED]['trackingExceptionEvidence']
        a['classes'][_ASSET_UNTRACKED]['trackingExceptionEvidence']=[
            e for e in evidence if e!='clonePullImpact']
    _asset_edit(root,apply)
_asset_mut('mutation_tracking_exception_evidence_reduced',mut_exception_evidence_reduced,
           '보존 제외 근거 항목이 빠졌다')

def mut_size_threshold_hardcoded(root):
    def apply(a): a['classes'][_ASSET_UNTRACKED]['sizeThreshold']='50MB 이상 제외'
    _asset_edit(root,apply)
_asset_mut('mutation_size_threshold_hardcoded',mut_size_threshold_hardcoded,
           '임의 용량 기준이 하드코딩됐다')

# Git LFS는 catalog-derived executable runtime에만 적용한다. 전역 JAR LFS나 metadata LFS로
# 되돌리는 것은 actual consumer clone에서 필요 없는 object까지 받거나 SHA 계약을 깨뜨린다.
_asset_mut('mutation_lfs_transport_not_adopted',
           lambda root: _asset_edit(root,lambda a: a['artifactClassification']['gitLfs'].__setitem__('adopted',False)),
           '확정된 Git LFS transport를 정본화하지 않았다')

def mut_lfs_runtime_reclassified(root):
    def apply(a):
        rule=next(r for r in a['artifactClassification']['rules'] if r['id']=='publicBinaryRuntimeExecutable')
        rule['assetClass']=_ASSET_TRACKED; rule['transport']='REGULAR_GIT'
    _asset_edit(root,apply)
_asset_mut('mutation_lfs_runtime_reclassified_as_regular_git',mut_lfs_runtime_reclassified,
           'binary runtime must use the canonical GIT_LFS class')

def mut_lfs_global_jar_scope(root):
    p=root/'.gitattributes'
    p.write_text(p.read_text(encoding='utf-8')+chr(10)+'*.jar filter=lfs diff=lfs merge=lfs -text'+chr(10),encoding='utf-8')
_asset_mut('mutation_lfs_global_jar_scope',mut_lfs_global_jar_scope,
           'runtime catalog executable만 exact하게 따라야 한다')

def mut_lfs_metadata_scope(root):
    p=root/'.gitattributes'
    p.write_text(p.read_text(encoding='utf-8')+chr(10)+'cpf-release/binary-repository/**/*.pom filter=lfs diff=lfs merge=lfs -text'+chr(10),encoding='utf-8')
_asset_mut('mutation_lfs_metadata_scope',mut_lfs_metadata_scope,
           'runtime catalog executable만 exact하게 따라야 한다')

def mut_lfs_validator_removed(root):
    (root/'cpf-tools/release/open-git/verify_release_lfs_contract.py').unlink()
_asset_mut('mutation_lfs_validator_removed',mut_lfs_validator_removed,
           'LFS attribute/materialization validator가 없다')

def mut_engine_skips_fresh_open_git_lfs_gate(root):
    p=root/'cpf-tools/release/open-git/cpf_open_git.py'
    t=p.read_text(encoding='utf-8')
    line='    open_git_lfs_result = verify_release_lfs_contract('+chr(10)
    if line not in t: raise AssertionError('fresh Open Git LFS gate not found')
    p.write_text(t.replace(line,'    # fresh Open Git LFS gate removed'+chr(10),1),encoding='utf-8')
_asset_mut('mutation_engine_skips_fresh_open_git_lfs_gate',mut_engine_skips_fresh_open_git_lfs_gate,
           'candidate binary, public staging, fresh Open Git tree')

# Canonical Source 를 Release 가 다시 만들거나 지난 출력을 이어 쓰는 되돌림.
_asset_mut('mutation_canonical_source_regenerated_by_release_engine',
           lambda root: _asset_edit(root,_class_set(_ASSET_CANONICAL,'freshRegenerationRequired',True)),
           '사람이 작성한 정본을 Release 가 다시 코드 생성한다')

_asset_mut('mutation_canonical_source_reuses_previous_release_output',
           lambda root: _asset_edit(root,_class_set(_ASSET_CANONICAL,'reusePreviousReleaseOutput',True)),
           '지난 Release tree 의 파일을 그대로 이어 쓴다')

def mut_canonical_verification_reduced(root):
    def apply(a):
        checks=a['classes'][_ASSET_CANONICAL]['verificationPolicy']
        a['classes'][_ASSET_CANONICAL]['verificationPolicy']=[
            c for c in checks if c!='freshConsumerExecution']
    _asset_edit(root,apply)
_asset_mut('mutation_canonical_source_verification_reduced',mut_canonical_verification_reduced,
           '매 Release 검증 항목이 빠졌다')

def mut_launcher_reclassified(root):
    model=_asset_model(root)
    for key in ('sourceRules','templateRules'):
        for rule in model.get(key,[]):
            if str(rule.get('target','')).startswith('bin/cpf'):
                rule['releaseAssetClass']=_ASSET_TRACKED
    _asset_write(root,model)
_asset_mut('mutation_launcher_reclassified_as_tracked_result',mut_launcher_reclassified,
           '공개 launcher 가 정본 Source 부류가 아니다')

def mut_engine_generates_launcher(root):
    p=root/'cpf-tools/release/open-git/cpf_open_git.py'
    p.write_text(p.read_text(encoding='utf-8')
                 +chr(10)+'def _emit_launcher(path):'+chr(10)
                 +'    path.write_text("#!/usr/bin/env sh")'+chr(10),encoding='utf-8')
_asset_mut('mutation_release_engine_generates_launcher_body',mut_engine_generates_launcher,
           'Release 엔진이 launcher 본문을 생성한다')

def mut_engine_classifies_by_extension(root):
    p=root/'cpf-tools/release/open-git/cpf_open_git.py'
    p.write_text(p.read_text(encoding='utf-8')
                 +chr(10)+'def _classify(name):'+chr(10)
                 +'    if name.endswith(".jar"): return "IGNORE"'+chr(10)
                 +'    return "TRACK"'+chr(10),encoding='utf-8')
_asset_mut('mutation_release_engine_classifies_by_extension',mut_engine_classifies_by_extension,
           'Release 엔진이 확장자로 보존/공개 정책을 정한다')

def mut_generator_input_removed(root):
    model=_asset_model(root)
    key=model['releaseAssetPolicy'].get('generatorInputKey','generatorInput')
    for rule in model.get('templateRules',[]):
        rule.pop(key,None)
    _asset_write(root,model)
_asset_mut('mutation_generator_input_declaration_removed',mut_generator_input_removed,
           '생성 입력을 선언한 자산이 하나도 없다')

def mut_promotion_before_verification(root):
    def apply(a):
        a['classes'][_ASSET_TRACKED]['currentizationOrder']=[
            'generate','currentizeTrackedSnapshot','verify','promote']
    _asset_edit(root,apply)
_asset_mut('mutation_promotion_before_verification',mut_promotion_before_verification,
           '검증 전에 tracked 결과를 덮어쓴다')

_asset_mut('mutation_artifact_set_currentized_non_atomically',
           lambda root: _asset_edit(root,_class_set(_ASSET_TRACKED,'atomicArtifactSet',False)),
           '반쪽 상태를 허용한다')

# Clean Workspace 계약의 되돌림.
def mut_clean_uses_git_clean(root):
    def apply(a):
        a['cleanWorkspace']['forbiddenCommands']=[
            c for c in a['cleanWorkspace']['forbiddenCommands'] if c!='git clean']
    _asset_edit(root,apply)
_asset_mut('mutation_release_clean_uses_git_clean',mut_clean_uses_git_clean,
           '광범위 destructive 명령이 금지되지 않았다')

def mut_clean_protects_nothing(root):
    def apply(a):
        a['cleanWorkspace']['protectedPaths']=[
            p for p in a['cleanWorkspace']['protectedPaths'] if p!='.gitignore']
    _asset_edit(root,apply)
_asset_mut('mutation_release_clean_protects_no_repository_authority',mut_clean_protects_nothing,
           '보호 경로가 빠졌다')

def mut_residue_allowed(root):
    def apply(a): a['cleanWorkspace']['previousResidueAllowed']=True
    _asset_edit(root,apply)
_asset_mut('mutation_previous_release_residue_allowed',mut_residue_allowed,
           '이전 Release 잔여물이 새 Release 에 살아남는 것을 허용한다')

def mut_destroy_before_prerequisites(root):
    def apply(a): a['cleanWorkspace']['prerequisitesBeforeDestruction']=False
    _asset_edit(root,apply)
_asset_mut('mutation_release_destroys_before_prerequisites',mut_destroy_before_prerequisites,
           '전제조건 확인 전에 직전 Release 를 지우는 계약이다')

def mut_generate_before_clean(root):
    # 삭제를 생성 뒤로 옮긴다. 이전 Release 잔여물이 새 Release 에 섞인다.
    p=root/'cpf-tools/release/open-git/cpf_open_git.py'
    t=p.read_text(encoding='utf-8')
    line='    release = clean_release_root(root)'+chr(10)
    if line not in t: raise AssertionError('clean 지점을 찾지 못했다')
    t=t.replace(line,'    release = _release_root_without_cleaning(root)'+chr(10),1)
    p.write_text(t,encoding='utf-8')
_asset_mut('mutation_engine_generates_before_cleaning_release_root',mut_generate_before_clean,
           'Release 재생성 지점을 찾지 못했다')

def mut_engine_reads_previous_release(root):
    p=root/'cpf-tools/release/open-git/cpf_open_git.py'
    t=p.read_text(encoding='utf-8')
    marker='    release_stage(3, '
    if marker not in t: raise AssertionError('stage 3 지점을 찾지 못했다')
    t=t.replace(marker,'    previous_release = root / "cpf-release" / "open-git"'+chr(10)+marker,1)
    p.write_text(t,encoding='utf-8')
_asset_mut('mutation_engine_reads_previous_release_as_input',mut_engine_reads_previous_release,
           '이전 Release 결과를 입력으로 읽는다')

def mut_publish_outside_staging(root):
    p=root/'cpf-tools/release/open-git/cpf_open_git.py'
    t=p.read_text(encoding='utf-8')
    line='    raw_repo = work / "binary-repository-raw"'
    if line not in t: raise AssertionError('staging repository 지점을 찾지 못했다')
    p.write_text(t.replace(line,'    raw_repo = release / "binary-repository"',1),encoding='utf-8')
_asset_mut('mutation_engine_publishes_outside_isolated_staging',mut_publish_outside_staging,
           '발행이 격리 staging repository 를 쓰지 않는다')

def mut_publisher_targets_open_git(root):
    def apply(a):
        a['publicationRouting']['forbiddenDirectTargets']=[
            t for t in a['publicationRouting']['forbiddenDirectTargets'] if t!='openGitWorkingTree']
    _asset_edit(root,apply)
_asset_mut('mutation_publisher_targets_open_git_tree',mut_publisher_targets_open_git,
           '발행 대상 금지 목록에 없다')

def mut_gitignore_blanket(root):
    p=root/'.gitignore'
    p.write_text(p.read_text(encoding='utf-8')+chr(10)+'*.jar'+chr(10),encoding='utf-8')
_asset_mut('mutation_gitignore_blanket_excludes_release_artifacts',mut_gitignore_blanket,
           'Current Verified Release Artifact 까지 일괄 제외한다')

# Gate 분리와 Acceptance 의 되돌림.
def mut_development_gate_full(root):
    def apply(a): a['developmentGate']='FULL_FRESH_EVERY_TIME'
    _asset_edit(root,apply)
_asset_mut('mutation_development_gate_forced_to_full_release',mut_development_gate_full,
           '개발 중에도 전체 Gate 를 반복하는 계약이다')

def mut_project_before_build(root):
    def apply(a):
        order=a['finalReleaseCandidateOrder']
        order.remove('canonicalPublicSourceProjection')
        order.insert(order.index('freshBuild'),'canonicalPublicSourceProjection')
    _asset_edit(root,apply)
_asset_mut('mutation_final_order_projects_before_build',mut_project_before_build,
           '투영이 Fresh Build 보다 먼저 온다')

def mut_single_acceptance(root):
    def apply(a): a['acceptance']['bothMustHold']=False
    _asset_edit(root,apply)
_asset_mut('mutation_single_acceptance_statement_allowed',mut_single_acceptance,
           '두 Acceptance 중 하나만 만족해도 되는 계약이다')

def mut_previous_release_copy(root):
    (root/'release-previous').mkdir(parents=True,exist_ok=True)
    (root/'release-previous'/'keep.txt').write_text('old release copy',encoding='utf-8')
_asset_mut('mutation_previous_release_copy_kept_in_working_tree',mut_previous_release_copy,
           '과거 Release 사본이 Working Tree 에 있다')

def mut_harness_asset_rule_removed(root):
    p=root/'cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md'
    p.write_text(p.read_text(encoding='utf-8').split('## 39. Release Asset')[0],encoding='utf-8')
_asset_mut('mutation_harness_release_asset_rule_removed',mut_harness_asset_rule_removed,
           'Current Harness 에 Release Asset 계약이 없다')

def mut_asset_registry_relation_removed(root):
    p=root/'cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv'
    p.write_text(p.read_text(encoding='utf-8').replace('test_cpf_release_asset_freshness_contract','REMOVED'),
                 encoding='utf-8')
_asset_mut('mutation_release_asset_registry_relation_removed',mut_asset_registry_relation_removed,
           'Registry 가 이 계약 Validator 를 참조하지 않는다')

# Service Registry provisioning 계약(Harness 40).
# "누가 등록하는가" 가 흐려지면 사용자가 만든 Domain 이 영원히 기동하지 못한다.
_SVC_TEST=ROOT/'cpf-tools/verification/tests/test_cpf_service_registry_provisioning_contract.py'
_SVC_FILES=[
    'cpf-tools/db/canonical/service-registry-provisioning.json',
    'cpf-tools/db/canonical/platform-schema.json',
    'cpf-tools/runtime/bootstrap/CpfBootstrap.java',
    'cpf-tools/release/open-git/open-git-surface-policy.json',
    'cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md',
    'cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv',
    'cpf-starters/platform-operations/runtime-control/src/main/java/com/cpf/platform/operations/runtimecontrol/CpfRuntimeControlAgentAutoConfiguration.java',
]
for _vendor in ('mariadb','oracle','postgresql'):
    for _name in ('service-registry-select.sql','service-registry-insert.sql',
                  'service-endpoint-select.sql','service-endpoint-insert.sql'):
        _SVC_FILES.append('cpf-tools/db/vendor/'+_vendor+'/runtime/cpf/repository/'+_name)
# 실재하는 Generated Domain 계약이 있어야 "이름을 복제하지 않는다" 를 실제로 판정할 수 있다.
for _contract in sorted(ROOT.glob('cpf-*/gradle.properties')):
    if 'cpf.domain.contractVersion=1' in _contract.read_text(encoding='utf-8').replace(' ',''):
        _SVC_FILES.append(_contract.parent.name+'/gradle.properties')
_SVC_PREFIX='cpf-service-registry-negative-'
_SVC_ENV='CPF_SERVICE_REGISTRY_ROOT'
_SVC_CONTRACT='cpf-tools/db/canonical/service-registry-provisioning.json'
_SVC_BOOTSTRAP='cpf-tools/runtime/bootstrap/CpfBootstrap.java'

def _svc_mut(name, mutate, expected):
    _contract_mut(name,_SVC_TEST,_SVC_PREFIX,_SVC_FILES,_SVC_ENV,mutate,expected)

def _svc_model(root):
    import json as _json
    return _json.loads((root/_SVC_CONTRACT).read_text(encoding='utf-8'))

def _svc_write(root, model):
    import json as _json
    (root/_SVC_CONTRACT).write_text(_json.dumps(model,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')

def _svc_edit(root, mutate):
    model=_svc_model(root); mutate(model); _svc_write(root,model)

_contract_positive('service_registry_provisioning_contract_positive_control',_SVC_TEST,
                   _SVC_PREFIX,_SVC_FILES,_SVC_ENV)

def mut_svc_contract_removed(root):
    (root/_SVC_CONTRACT).unlink()
_svc_mut('mutation_service_registry_contract_removed',mut_svc_contract_removed,
         'Service Registry provisioning 계약 정본이 없다')

_svc_mut('mutation_service_registry_owner_becomes_the_generator',
         lambda root: _svc_edit(root, lambda m: m.__setitem__('executor','cpf domain-new generator')),
         '등록의 실행 주체가 bootstrap 이 아니다')

def mut_svc_required_column_unprovided(root):
    _svc_edit(root, lambda m: m['valueSources'].pop('service_name',None))
_svc_mut('mutation_service_registry_required_column_unprovided',mut_svc_required_column_unprovided,
         '필수 column 에 값을 주지 않는다')

def mut_svc_identity_inferred(root):
    def apply(m):
        m['valueSources']['service_id']={'from':'domainContract','key':'cpf.domain.name','transform':'NONE'}
    _svc_edit(root,apply)
_svc_mut('mutation_service_registry_identity_inferred_from_domain_name',mut_svc_identity_inferred,
         'canonical SystemCode 가 아니다')

def mut_svc_transform_fallback(root):
    def apply(m):
        m['transformPolicy']['allowed']=['NONE','FALLBACK']
        m['transformPolicy']['forbidden']=[x for x in m['transformPolicy']['forbidden'] if x!='FALLBACK']
    _svc_edit(root,apply)
_svc_mut('mutation_service_registry_transform_allows_fallback',mut_svc_transform_fallback,
         '금지 변환 선언이 빠졌다')

def mut_svc_targets_enumerated(root):
    import re as _re
    names=[]
    for properties in root.glob('cpf-*/gradle.properties'):
        text=properties.read_text(encoding='utf-8')
        m=_re.search(r'^cpf\\.domain\\.systemCode\\s*=\\s*(\\S+)',text,_re.M)
        if m: names.append(m.group(1))
    if not names: names=['MBR']
    _svc_edit(root, lambda m: m['appliesTo'].__setitem__('enumeratedDomains',names))
_svc_mut('mutation_service_registry_targets_enumerated_domains',mut_svc_targets_enumerated,
         '계약이 특정 Domain 이름을 담고 있다')

_svc_mut('mutation_service_registry_conflict_overwrites_existing_row',
         lambda root: _svc_edit(root, lambda m: m['reconcile'].__setitem__('neverOverwriteExistingRow',False)),
         '기존 Registry row 를 덮어쓸 수 있는 계약이다')

_svc_mut('mutation_service_registry_disabled_row_silently_enabled',
         lambda root: _svc_edit(root, lambda m: m['reconcile'].__setitem__('disabledRow','AUTO_ENABLE')),
         '조용히 다시 켠다')

_svc_mut('mutation_service_registry_runtime_self_registration_allowed',
         lambda root: _svc_edit(root, lambda m: m['reconcile'].__setitem__('runtimeSelfRegistration','ALLOWED')),
         'Runtime 자가 등록을 허용한다')

_svc_mut('mutation_service_registry_rerun_not_idempotent',
         lambda root: _svc_edit(root, lambda m: m['reconcile'].__setitem__('idempotentOnRerun',False)),
         'bootstrap 재실행이 안전하다고 선언하지 않았다')

_svc_mut('mutation_service_registry_profile_specific_behaviour',
         lambda root: _svc_edit(root, lambda m: m.__setitem__('profileInvariant',False)),
         'profile 별로 다른 lifecycle 을 쓰는 계약이다')

def mut_endpoint_provisioning_removed(root):
    _svc_edit(root, lambda m: m.pop('endpointTable',None))
_svc_mut('mutation_service_endpoint_provisioning_removed',mut_endpoint_provisioning_removed,
         'endpoint provisioning 계약이 없다')

def mut_endpoint_code_diverges(root):
    def apply(m):
        m['endpointTable']['valueSources']['endpoint_code']['pattern']='{service_id}_ENDPOINT'
    _svc_edit(root,apply)
_svc_mut('mutation_service_endpoint_code_diverges_from_runtime',mut_endpoint_code_diverges,
         'Runtime Control 과 다른 endpoint code 를 쓴다')

def mut_endpoint_scope_overwrites(root):
    _svc_edit(root, lambda m: m['endpointTable']['reconcile'].__setitem__('scopeConflict','OVERWRITE'))
_svc_mut('mutation_service_endpoint_scope_conflict_overwrites',mut_endpoint_scope_overwrites,
         '같은 endpoint_code 가 다른 service 에 있어도 넘어간다')

def mut_endpoint_not_wired(root):
    p=root/_SVC_BOOTSTRAP
    t=p.read_text(encoding='utf-8')
    line='        int endpoints = reconcileServiceEndpoints(model);'+chr(10)
    if line not in t: raise AssertionError('endpoint 정합 호출을 찾지 못했다')
    p.write_text(t.replace(line,'        int endpoints = 0;'+chr(10),1),encoding='utf-8')
_svc_mut('mutation_service_endpoint_reconcile_not_wired',mut_endpoint_not_wired,
         'endpoint 정합이 service 정합과 같은 lifecycle 에 있지 않다')

def mut_svc_vendor_sql_removed(root):
    (root/'cpf-tools/db/vendor/oracle/runtime/cpf/repository/service-registry-insert.sql').unlink()
_svc_mut('mutation_service_registry_vendor_sql_removed',mut_svc_vendor_sql_removed,
         'vendor pack 에 없는 Service Registry SQL')

def mut_svc_sql_duplicated(root):
    p=root/_SVC_BOOTSTRAP
    p.write_text(p.read_text(encoding='utf-8')
                 +chr(10)+'// duplicated authority'+chr(10)
                 +'final class CpfServiceRegistrySqlCopy { static final String INSERT = "INSERT INTO OPS_SERVICE(service_id) VALUES (?)"; }'+chr(10),
                 encoding='utf-8')
_svc_mut('mutation_service_registry_sql_duplicated_in_bootstrap',mut_svc_sql_duplicated,
         'Service Registry SQL 을 코드에 복제한다')

def mut_svc_not_wired(root):
    p=root/_SVC_BOOTSTRAP
    t=p.read_text(encoding='utf-8')
    p.write_text(t.replace('        reconcileServiceRegistry();'+chr(10),'',1),encoding='utf-8')
_svc_mut('mutation_service_registry_reconcile_not_wired_into_bootstrap',mut_svc_not_wired,
         'DB Lifecycle 이 Service Registry 를 맞추지 않는다')

def mut_svc_domain_hardcoded(root):
    import re as _re
    names=[]
    for properties in root.glob('cpf-*/gradle.properties'):
        text=properties.read_text(encoding='utf-8')
        m=_re.search(r'^cpf\\.domain\\.systemCode\\s*=\\s*(\\S+)',text,_re.M)
        if m: names.append(m.group(1))
    if not names: names=['MBR']
    p=root/_SVC_BOOTSTRAP
    t=p.read_text(encoding='utf-8')
    marker='            String serviceId = d.systemCode;'
    if marker not in t: raise AssertionError('reconcile 본문을 찾지 못했다')
    p.write_text(t.replace(marker,marker+chr(10)
                           +'            if (serviceId.equals("'+names[0]+'")) serviceId = serviceId.trim();',1),
                 encoding='utf-8')
_svc_mut('mutation_service_registry_domain_name_hardcoded_in_bootstrap',mut_svc_domain_hardcoded,
         'reconcile 이 Domain 이름을 하드코딩한다')

def mut_svc_not_projected(root):
    import json as _json
    p=root/'cpf-tools/release/open-git/open-git-surface-policy.json'
    policy=_json.loads(p.read_text(encoding='utf-8'))
    for key in ('sourceRules','templateRules'):
        policy[key]=[r for r in policy.get(key,[])
                     if str(r.get('target',''))!='config/service-registry-provisioning.json']
    p.write_text(_json.dumps(policy,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')
_svc_mut('mutation_service_registry_contract_not_projected_to_public',mut_svc_not_projected,
         'provisioning 계약이 공개 배포본에 투영되지 않는다')

def mut_svc_harness_rule_removed(root):
    p=root/'cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md'
    p.write_text(p.read_text(encoding='utf-8').split('## 40. Service Registry')[0],encoding='utf-8')
_svc_mut('mutation_harness_service_registry_rule_removed',mut_svc_harness_rule_removed,
         'Current Harness 에 Service Registry provisioning 계약이 없다')

def mut_svc_registry_relation_removed(root):
    p=root/'cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv'
    p.write_text(p.read_text(encoding='utf-8').replace('test_cpf_service_registry_provisioning_contract','REMOVED'),
                 encoding='utf-8')
_svc_mut('mutation_service_registry_registry_relation_removed',mut_svc_registry_relation_removed,
         'Registry 가 이 계약 Validator 를 참조하지 않는다')

# 사용자 결정(A) 이후의 tracking 예외 계약. 크기 하나로 결론내지 않는다는 규칙을 지킨다.

def _asset_rule(model, rule_id):
    for rule in model['artifactClassification']['rules']:
        if rule['id'] == rule_id: return rule
    raise AssertionError('rule not found: ' + rule_id)

def mut_gitignore_excludes_metadata(root):
    # binary-repository 전체를 다시 제외한다. POM/checksum/manifest 까지 사라진다.
    p=root/'.gitignore'
    p.write_text(p.read_text(encoding='utf-8')+chr(10)+'/cpf-release/binary-repository/'+chr(10),encoding='utf-8')
_asset_mut('mutation_gitignore_excludes_release_metadata_too',mut_gitignore_excludes_metadata,
           'Current Verified Release Artifact 까지 일괄 제외한다')

def mut_unknown_artifact_allowed(root):
    _asset_edit(root, lambda a: a['artifactClassification'].__setitem__('unknownArtifact','ALLOW'))
_asset_mut('mutation_unknown_artifact_silently_allowed',mut_unknown_artifact_allowed,
           '분류가 없는 Artifact 를 통과시킨다')

def mut_threshold_replaces_measurement(root):
    def apply(a):
        _asset_rule(a,'publicBinaryRuntimeExecutable')['sizeThresholdMb']='50MB 이상 제외'
    _asset_edit(root,apply)
_asset_mut('mutation_lfs_size_threshold_replaces_measurement',mut_threshold_replaces_measurement,
           '임의 용량 기준이 하드코딩됐다')

def mut_lfs_runtime_dropped_from_public(root):
    def apply(a):
        _asset_rule(a,'publicBinaryRuntimeExecutable')['publicRelease']=False
    _asset_edit(root,apply)
_asset_mut('mutation_lfs_runtime_dropped_from_public_release',mut_lfs_runtime_dropped_from_public,
           'GIT_LFS runtime must remain publicly delivered')

def mut_payload_rule_removed(root):
    p=root/'cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md'
    text=p.read_text(encoding='utf-8')
    head,_,tail=text.partition('### 39.6 Release Size Finding')
    p.write_text(head+('### 39.7'+tail.split('### 39.7',1)[1] if '### 39.7' in tail else ''),encoding='utf-8')
_asset_mut('mutation_payload_composition_rule_removed',mut_payload_rule_removed,
           'payload composition 으로 판정하라는 Rule 이 없다')

def mut_payload_tool_total_only(root):
    p=root/'cpf-tools/release/open-git/report_release_payload_composition.py'
    t=p.read_text(encoding='utf-8')
    p.write_text(t.replace('duplicateEmbeddedDependencyBytes','totalBytesOnly')
                  .replace('ossSeparatelyVendoredBytes','totalBytesOnly2'),encoding='utf-8')
_asset_mut('mutation_payload_tool_reports_only_total_size',mut_payload_tool_total_only,
           '보고 항목이 빠졌다')

failed=[x for x in checks if not x[1]]
print(f'NEGATIVE_FIXTURES_FINAL={len(checks)-len(failed)}/{len(checks)} PASS group={_NEG_GROUP}',flush=True)
# Explicitly remove the reusable scratch fixture before interpreter shutdown. This avoids
# platform-dependent TemporaryDirectory finalizer delays while keeping repository garbage=0.
shutil.rmtree(_NEG_ROOT, ignore_errors=True)
raise SystemExit(1 if failed else 0)
