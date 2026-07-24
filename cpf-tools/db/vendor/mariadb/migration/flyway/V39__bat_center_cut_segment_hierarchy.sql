-- CPF V39
-- BAT Center-Cut transaction hierarchy alignment.
-- Final runtime identity: transaction_id + transaction_segment_id + parent_segment_id.

ALTER TABLE bat_center_cut_item
    ADD COLUMN IF NOT EXISTS parent_segment_id VARCHAR(120) NULL
        COMMENT '부모 센터컷/Worker 실행 구간 ID'
        AFTER transaction_segment_id;

CREATE INDEX IF NOT EXISTS ix_bat_center_cut_item_parent_segment
    ON bat_center_cut_item(parent_segment_id);

ALTER TABLE bat_center_cut_result
    ADD COLUMN IF NOT EXISTS parent_segment_id VARCHAR(120) NULL
        COMMENT '부모 센터컷/Worker 실행 구간 ID'
        AFTER transaction_segment_id;

CREATE INDEX IF NOT EXISTS ix_bat_center_cut_result_parent_segment
    ON bat_center_cut_result(parent_segment_id);
