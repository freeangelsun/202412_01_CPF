# CPF 문서·Evidence Governance Guide

## 1. Single Source of Truth

각 역할은 하나의 정본만 가진다.

## 2. Markdown First

구현 중:

- Markdown
- Source-linked
- Diff-friendly

최종:

- DOCX/PDF generated
- Content comparison
- Versioned

## 3. README

제품 입구만 담당한다.

## 4. Work/Review

진행·Gap·검수는 `cpf-docs`에 둔다.

## 5. Guide Quality

Guide는 다음을 포함한다.

- 목적
- Minimal Example
- Default
- Property
- Option
- Error
- Limit
- Security
- Test
- Operations
- Migration

## 6. Evidence

Runtime Evidence와 Static Inventory를 분리한다.

## 7. Stale

Source/API/SQL/UI 변경 후 Evidence·Guide를 재검증한다.

## 8. Link Gate

- Broken Link
- Old Package
- Old SystemCode
- Old Property
- Old Screen
- Old SQL
- Old Command

## 9. No Duplication

날짜별 복제 문서를 정본으로 만들지 않는다.
