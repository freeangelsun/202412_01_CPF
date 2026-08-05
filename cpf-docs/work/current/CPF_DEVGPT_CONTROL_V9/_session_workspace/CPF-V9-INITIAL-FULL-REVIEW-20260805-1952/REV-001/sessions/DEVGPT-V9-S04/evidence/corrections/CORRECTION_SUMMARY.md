# Correction Summary

Baseline: `fc207ac5560da59f352ee0c5f83199177f2987b4`

1. Initial applied-root DB test failed because the existing legacy `CmnTemplateRepository` sources were present while the test incorrectly required physical deletion. Deletion was not performed. The legacy JDBC adapter was neutralized as a deprecated, non-bean, fail-closed compatibility shell and the test now asserts no active duplicate provider.
2. Java harness then exposed a channel escaping regression in the consolidated renderer. HTML/JSON escaping and the custom-channel exactly-one escaper contract were restored while malformed tokens remain fail-closed.
3. Lifecycle contract initially failed only because the subset fixture omitted existing exact-SHA static-gate scripts and vendor pack files. Those baseline files were restored to the applied simulation root; the product overlay did not add fake baseline files. Re-execution passed.
4. Final revalidation: DB unit 68/68, Script unit 34/34, Python compile, Java compile/harness, semantic parity, schema governance, vendor manifest, lifecycle/development contract and CMN query contract all Exit 0.
