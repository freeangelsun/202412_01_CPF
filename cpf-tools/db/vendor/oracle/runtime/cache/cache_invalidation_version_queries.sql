-- CPF cache durable version-fence operational query. Bind parameters are framework-owned.
SELECT LAST_VERSION, UPDATED_AT
FROM CPF_CACHE_INVALIDATION_VERSION
WHERE CONSUMER_ID = ? AND TENANT_ID = ? AND NAMESPACE_NAME = ? AND CACHE_KEY_VALUE = ?;
