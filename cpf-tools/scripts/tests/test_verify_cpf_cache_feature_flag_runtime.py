from __future__ import annotations
import importlib.util
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[1]/'verify-cpf-cache-feature-flag-runtime.py'
def load():
 spec=importlib.util.spec_from_file_location('cache_ff_gate',SCRIPT); m=importlib.util.module_from_spec(spec); spec.loader.exec_module(m); return m
def test_gate_declares_exact_runtime_and_consumer_contracts():
 m=load(); assert len(m.CACHE_FILES)==9; assert len(m.FEATURE_FILES)==10; assert len(m.STATIC_PATHS)==12; assert 'CpfLocalCacheProvider.java' in ' '.join(m.CACHE_FILES); assert 'CpfFeatureFlagRuntime.java' in ' '.join(m.FEATURE_FILES); assert 'AdmCacheOperationService.java' in ' '.join(m.STATIC_PATHS)
