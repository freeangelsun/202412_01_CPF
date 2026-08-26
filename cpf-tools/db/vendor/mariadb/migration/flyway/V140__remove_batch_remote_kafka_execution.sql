-- CPF V140: remove retired Batch Kafka Remote Execution ledger.
-- General Batch/Worker/Scheduler/Center-Cut do not use Kafka Remote Execution.
DROP TABLE IF EXISTS BAT_REMOTE_MESSAGE_LEDGER;
