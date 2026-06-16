-- Cleanup legacy local/test sample layout.
-- Earlier CPF sample scripts created CMN-owned reference tables and member samples outside their domains.
-- The current layout owns framework reference tables in pfwDB and MBR samples in mbrDB.member only.

DROP DATABASE IF EXISTS cmnDB;
DROP TABLE IF EXISTS admDB.member;
DROP TABLE IF EXISTS accDB.acc_member;
