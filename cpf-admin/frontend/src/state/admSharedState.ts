import { reactive } from "vue";
import { createAdmState, resetAdmSensitiveState } from "./createAdmState";

// App Shell과 lazy-loaded feature panel이 동일 운영 상태를 공유한다.
// feature component가 다시 mount되어도 API filter/form/result 상태가 분리되지 않는다.
export const admSharedState = reactive(createAdmState()) as any;
admSharedState.resetSensitiveState = () => resetAdmSensitiveState(admSharedState);
