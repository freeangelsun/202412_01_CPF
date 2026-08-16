-- Ownership rollback guard: legacy ADM MEMBER CRUD must not be automatically restored.
-- Restore only through a reviewed migration if the product ownership policy itself changes.
SELECT 'R41 requires explicit reviewed ownership rollback' AS rollback_guard;
