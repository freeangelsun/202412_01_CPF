/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DetectedTransaction {
  apiPath?: string;
  controllerClass?: string;
  domainCode?: string;
  handlerMethod?: string;
  httpMethod?: string;
  id?: string;
  moduleCode?: string;
  name?: string;
  operationId?: string;
}
