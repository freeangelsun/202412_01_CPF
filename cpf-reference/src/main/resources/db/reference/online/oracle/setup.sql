CREATE TABLE cpf_ref_online_abcd (
  business_key VARCHAR2(128 CHAR) NOT NULL,
  value_text VARCHAR2(1000 CHAR),
  CONSTRAINT pk_cpf_ref_online_abcd PRIMARY KEY (business_key)
);
