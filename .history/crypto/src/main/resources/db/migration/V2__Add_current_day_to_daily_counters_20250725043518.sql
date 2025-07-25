-- V2__Add_current_day_to_daily_counters.sql
-- Add missing current_day column to daily_counters table

ALTER TABLE daily_counters ADD COLUMN current_day INT NOT NULL DEFAULT 1; 