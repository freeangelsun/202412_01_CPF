# CPF Documentation Next Session Handover — 2.9.0

Current canonical documentation is the unversioned `cpf-docs/governance/documentation-harness/` plus README and 11 DOCX/PDF pairs. This session used PATCH_FIRST: good 2.9.0 content/visuals were retained and user findings were applied as incremental quality uplift. Do not fresh-rewrite the documents unless the user explicitly requests it.

Key regression gates now cover README section breathing, 900/1200/1440 header wrap, dark table header text contrast, low-density/orphan pages, visual safe-area clipping and approved-baseline regression. User visual findings override automated false-green.

Current counts: README H2 12 / Visual 9; DOCX 11; PDF 11; 101 pages; negative fixtures 64.

Windows PowerShell wrappers and Windows VS Code built-in Markdown Preview remain environment-specific `미검증` from Linux.

After any future user finding: update Harness rule/profile/validator/negative fixture first, then PATCH only affected artifacts, re-render every changed DOCX/PDF/README surface, and fresh-replay the delivery ZIP.
