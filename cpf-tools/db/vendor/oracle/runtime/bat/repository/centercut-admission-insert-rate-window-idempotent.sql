MERGE INTO bat_center_cut_rate_window target
USING (
    SELECT ? AS center_cut_execution_id,
           ? AS window_second
      FROM dual
) source
ON (
    target.center_cut_execution_id = source.center_cut_execution_id
    AND target.window_second = source.window_second
)
WHEN NOT MATCHED THEN INSERT (
    center_cut_execution_id,
    window_second,
    admitted_count
) VALUES (
    source.center_cut_execution_id,
    source.window_second,
    0
)
