-- CPF source-compatible fix migration generated from canonical schemaVersion 37.
-- CPF_LOGICAL_DATABASE=cmnDB
-- Historical V74-V76 are immutable; all post-QA corrections are applied here.

ALTER TABLE cmn_business_calendar_day MODIFY (reason NULL);
ALTER TABLE cmn_business_calendar_day MODIFY (institution_code NULL);
