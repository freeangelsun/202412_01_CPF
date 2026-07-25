package com.cpf.core.api.reliability;

import java.util.Map;

/** 실제 자동복구 조치는 Owner Module이 구현하는 Port입니다. */
@FunctionalInterface
public interface CpfSelfHealingActionPort {
    ActionResult execute(ActionCommand command);

    record ActionCommand(
            String targetKey,
            String actionType,
            String reason,
            String approvalReference,
            Map<String, String> attributes) {
        public ActionCommand {
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        }
    }

    record ActionResult(boolean success, String message) {
        public static ActionResult succeeded(String message) { return new ActionResult(true, message); }
        public static ActionResult failed(String message) { return new ActionResult(false, message); }
    }
}
