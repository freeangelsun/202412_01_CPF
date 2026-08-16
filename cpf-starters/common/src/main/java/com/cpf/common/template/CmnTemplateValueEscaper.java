package com.cpf.common.template;

/**
 * Extension contract for escaping values in template channels not owned by the
 * built-in HTML, JSON, and plain-text policies.
 */
public interface CmnTemplateValueEscaper {
    /** Returns whether this extension owns the supplied channel. */
    boolean supports(String channel);

    /** Escapes one validated variable value for the supplied channel. */
    String escape(String channel, String variableName, String value);
}
