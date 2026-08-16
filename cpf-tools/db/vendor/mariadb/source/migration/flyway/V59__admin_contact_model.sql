-- ADM/BZA contact model separation.
-- ADM authentication Identity remains in adm_operator; contact data belongs to adm_operator_profile Directory/Profile.
-- Phone values are textual so country code/leading zero are preserved.

USE admDB;

ALTER TABLE adm_operator_profile
    ADD COLUMN IF NOT EXISTS MOBILE_NO VARCHAR(50) NULL
        COMMENT '연락처(휴대폰); 국가번호/선행 0 보존' AFTER EMAIL,
    ADD COLUMN IF NOT EXISTS OFFICE_PHONE_NO VARCHAR(50) NULL
        COMMENT '내부 전화번호/내선' AFTER MOBILE_NO;

USE bzaDB;

ALTER TABLE bza_employee
    ADD COLUMN IF NOT EXISTS office_phone_no VARCHAR(50) NULL
        COMMENT '내부 전화번호/내선; 휴대폰 연락처와 분리' AFTER mobile_no;
