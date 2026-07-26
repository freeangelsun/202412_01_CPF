SELECT COUNT(*)
  FROM bat_schedule_trigger
 WHERE schedule_id = ?
   AND scheduled_fire_at = ?
