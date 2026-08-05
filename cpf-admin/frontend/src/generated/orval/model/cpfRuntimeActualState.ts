/** Runtime agent actual state report. */
export interface CpfRuntimeActualState {
  changeType?: string;
  actualVersion: number;
  actualHash?: string;
  sourceDeliveryId?: string;
}
