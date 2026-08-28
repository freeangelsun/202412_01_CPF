# Open Issues

## Environment-specific verification only

1. **Windows PowerShell wrapper runtime — 미검증**
   - `APPLY.ps1`, `DELETE_ONLY.ps1`, `VERIFY.ps1` are packaged and their Windows root-containment implementation is statically gated.
   - Current environment is Linux, therefore native Windows PowerShell execution was not fabricated as PASS.

2. **Windows VS Code built-in Markdown Preview — 미검증**
   - 900/1200/1440 browser-width README rendering passes with all 9 local visuals.
   - The previously reported `Could not register service worker / InvalidStateError` is a VS Code WebView startup failure before Markdown content rendering. A Windows VS Code runtime is required to reproduce/confirm it.

There are **no known implementable document/harness/PDF/DOCX defects left open** in the current container verification scope.
