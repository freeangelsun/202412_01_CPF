# Baseline and Scope Delta

1. Initial checkpoint used `2b259ea...` and incorrectly limited scope to 156 unrelated Gateway IDs.
2. The first correction restored 20 capabilities but omitted `GWY-ENTRY`.
3. V7.1 `WORK_ITEM_INDEX.csv → 30_GATEWAY_EXTERNAL_INTEGRATION.md / 31_EVENT_MESSAGING_SAGA.md → ledger_part` revalidation established 21 assigned canonical capabilities.
4. Exact scope is 1,281 CPF-FR and 1,886 CPF-SC; `GWY-ENTRY` contributes 61 CPF-FR and 26 CPF-SC.
5. Current master is `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c`. Intervening changes were V7.1 documentation only; assigned product paths had no upstream delta.
6. Existing Messaging/TCP/DLQ and Gateway fixes were preserved and revalidated; no prior evidence was discarded.
