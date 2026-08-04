# QA-12E Open Issues

1. **Partition boundary mismatch**
   - Assigned logical order: 10,187–12,733
   - Actual first: `CPF-FR-012539` / `05-00012539`
   - Actual last: `CPF-FR-008247` / `06-00008247`
   - The expected suffix range `CPF-FR-010187`–`CPF-FR-012733` is not the actual partition set.
2. **Local clean clone unavailable**
   - `git clone` failed with `Could not resolve host: github.com` (exit 128).
3. **GitHub code search incomplete**
   - Lock/Lease searches returned `incomplete_results=true`; absence cannot be treated as conclusive source absence.
4. **Scenario master not yet assembled**
   - Scenario count and linked scenario set remain unverified in this checkpoint.
5. **Runtime/toolchain validation not executed**
   - No clean working copy exists in this runtime; Gradle/Java/DB/Browser tests cannot be attributed to exact HEAD yet.
