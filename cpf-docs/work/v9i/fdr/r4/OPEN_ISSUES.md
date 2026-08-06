# REV-004 R4 Open Issues

There is no known unresolved source implementation defect in the five Development GPT rows after the final static and isolated runtime gates. The following **verification conditions** remain open and must not be called PASS:

1. Apply this Root Overlay to a clean checkout at `a8be27a34bdac0b7c075e06d6e86571244c96421` and confirm no unrelated working-tree changes.
2. Run root Gradle with project-required Java 25, including clean/check/test and publication/consumer gates.
3. Run full frontend verification on Node >=22.18.0 with installed lockfile dependencies, including Playwright.
4. Run Pester and live Oracle/PostgreSQL/MariaDB lifecycle with non-production credentials and redacted evidence.
5. Run QA38/QA39 on the complete checkout rather than the overlay-only partial tree.
6. Obtain explicit user/QA approval before deleting `cpf-starters/openapi-webmvc`; no deletion is included.
7. QA final status remains pending.

External limitation: direct DNS access to GitHub was unavailable in this container; GitHub Connector provided the exact master identity and source reads.
