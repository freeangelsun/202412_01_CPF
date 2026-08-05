/** Common configuration create/update input. The authenticated operator is resolved from the server session; requestUser is not accepted. */
export interface CommonConfigRequest {
  configId?: number;
  configKey: string;
  configValue: string;
  configType?: "STRING" | "NUMBER" | "BOOLEAN" | "JSON";
  description?: string;
  encryptedYn?: "Y" | "N";
  useYn?: "Y" | "N";
  reason: string;
}
