/** Common code create/update input. The authenticated operator is resolved from the server session; requestUser is not accepted. */
export interface CommonCodeRequest {
  codeId?: number;
  parentId?: number;
  codeKey: string;
  codeValue: string;
  description?: string;
  useYn?: "Y" | "N";
  reason: string;
}
