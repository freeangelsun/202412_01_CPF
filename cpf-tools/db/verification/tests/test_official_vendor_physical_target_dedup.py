from __future__ import annotations

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[4]
RUNNER = ROOT / "cpf-tools/db/tools/invoke-official-db-vendor-sql.ps1"


class OfficialVendorPhysicalTargetDedupTest(unittest.TestCase):
    def test_consolidated_profile_aliases_execute_each_physical_target_once(self) -> None:
        source = RUNNER.read_text(encoding="utf-8-sig")
        self.assertIn("Get-PhysicalLifecycleTargetKey", source)
        self.assertIn("$seenPhysicalTargets", source)
        self.assertIn("[StringComparer]::OrdinalIgnoreCase", source)
        self.assertIn("$targets=@($physicalTargets)", source)
        self.assertIn("SKIP_DUPLICATE_TARGET", source)
        for identity_field in (
            "host",
            "port",
            "databaseName",
            "schemaName",
            "logicalDatabase",
            "adminUsername",
            "migrationUsername",
            "runtimeUsername",
        ):
            self.assertIn(f"Target.{identity_field}", source)

    def test_dedup_does_not_narrow_the_requested_module_inventory(self) -> None:
        source = RUNNER.read_text(encoding="utf-8-sig")
        filter_position = source.index("if($Modules.Count -gt 0)")
        dedup_position = source.index("function Get-PhysicalLifecycleTargetKey")
        execution_position = source.index("foreach($t in $targets)")
        self.assertLess(filter_position, dedup_position)
        self.assertLess(dedup_position, execution_position)
        self.assertNotIn("common/admin/batch", source[filter_position:dedup_position])


if __name__ == "__main__":
    unittest.main()
