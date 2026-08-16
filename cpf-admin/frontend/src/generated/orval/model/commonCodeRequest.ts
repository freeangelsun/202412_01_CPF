/** Code create/update input; operator is server-derived. */
export interface CommonCodeRequest { codeId?: number; parentId?: number; codeKey: string; codeValue: string; description?: string; useYn?: "Y" | "N"; reason: string; }
