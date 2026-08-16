/* CPF ORT deny policy. SPDX identifiers must also pass verify-cpf-supply-chain.py. */
val deniedLicenses = setOf(
    "AGPL-3.0-only",
    "GPL-2.0-only",
    "GPL-3.0-only",
    "SSPL-1.0",
    "BUSL-1.1",
    "NOASSERTION"
)

fun PackageRule.LicenseRule.isDeniedByCpf() = object : RuleMatcher {
    override val description = "isDeniedByCpf($license)"
    override fun matches() = license.toString() in deniedLicenses
}

fun RuleSet.deniedLicenseRule() = packageRule("CPF_DENIED_LICENSE") {
    require {
        -isExcluded()
    }
    licenseRule("CPF_DENIED_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED) {
        require {
            -isExcluded()
            +isDeniedByCpf()
        }
        error(
            "The package ${pkg.metadata.id.toCoordinates()} uses denied license $license.",
            "Replace the dependency or obtain an explicit legal approval before distribution."
        )
    }
}

val ruleSet = ruleSet(ortResult, licenseInfoResolver, resolutionProvider) {
    deniedLicenseRule()
}
ruleViolations += ruleSet.violations
