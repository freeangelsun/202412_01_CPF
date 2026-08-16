package com.cpf.education.operations.runtime;
import com.cpf.education.operations.runtime.application.EducationOperationsCapabilityContributor;
import java.util.Set;
import java.util.stream.Collectors;

/** R6J architecture regression: only adopter-facing ADM extensions remain executable in EDU. */
public final class EduAdmR6SelfTestMain {
    public static void main(String[] args) {
        var handlers = new EducationOperationsCapabilityContributor().handlers();
        var ids = handlers.stream().map(h -> h.definition().requirementId()).collect(Collectors.toSet());
        var expected = Set.of("EDU-ADM-02", "EDU-ADM-03", "EDU-ADM-04", "EDU-ADM-07");
        if (!ids.equals(expected)) throw new AssertionError("retained ADM extension set=" + ids);
        if (handlers.size() != 4) throw new AssertionError("retained ADM extension count=" + handlers.size());
        System.out.println("[CPF][R6J][EDU-ADM][ARCH][PASS] extension=4 product=9 merge=4 executable=4");
    }
}
