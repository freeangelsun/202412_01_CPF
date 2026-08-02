# POST-QA37 Overlay V2 Correction Note

- Current reviewed baseline: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
- Canonical Product Requirement count: **169**
- The prior PowerShell command returned 177 because it counted 8 Legacy Alias rows outside section 22.
- Section 22 alone contains 169 unique canonical IDs.
- Added read-only protection for other GPT-owned directories.
- Added exact verification and safe staging scripts.
- No protected path is contained in this overlay.
- No Source/DB/Runtime validation is claimed by this correction.
