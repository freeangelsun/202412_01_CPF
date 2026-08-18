/** Configuration create/update input; operator is server-derived. */
export interface CommonConfigRequest { configId?: number; configKey: string; configValue: string; configType?: "STRING" | "NUMBER" | "BOOLEAN" | "JSON"; description?: string; encryptedYn?: "Y" | "N"; useYn?: "Y" | "N"; reason: string; }
