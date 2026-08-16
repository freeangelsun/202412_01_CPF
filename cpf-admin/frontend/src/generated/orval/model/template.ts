import type { AdmFileJobConsumer } from './admFileJobConsumer';
import type { CpfTabularSchema } from './cpfTabularSchema';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Template {
  approvalRequired: boolean;
  atomicApply: boolean;
  consumer?: AdmFileJobConsumer;
  rollbackSupported: boolean;
  schema?: CpfTabularSchema;
}
