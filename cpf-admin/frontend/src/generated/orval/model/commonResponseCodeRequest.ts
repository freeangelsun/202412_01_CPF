/** Response-code create/update input. The authenticated operator is resolved from the server session; requestUser is not accepted. */
export interface CommonResponseCodeRequest {
  responseCode: string;
  messageCode: string;
  resultType: "S" | "E";
  moduleId: string;
  responseGroup: string;
  sequenceNo: string;
  httpStatus: number;
  description?: string;
  useYn?: "Y" | "N";
}
