package cpf.pfw.common.exception;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CPF 硫붿떆吏 ?쒗뵆由우쓽 ?숈쟻 媛믪쓣 移섑솚?섎뒗 怨듯넻 ?щ㎎?곗엯?덈떎.
 *
 * <p>CMN 硫붿떆吏 ?뚯씠釉붿씠???덉쇅 ?앹꽦?먯뿉??{@code {fieldName}} 媛숈? ?뚮젅?댁뒪??붾? ?ъ슜?섎㈃,
 * ?쒖? ?덉쇅 泥섎━? 嫄곕옒 濡쒓렇媛 媛숈? 洹쒖튃?쇰줈 硫붿떆吏瑜?議곕┰?⑸땲??</p>
 */
public final class CpfMessageFormatter {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Za-z0-9_.-]+)}");

    private CpfMessageFormatter() {
    }

    /**
     * 硫붿떆吏 ?쒗뵆由??덉쓽 {@code {key}} 媛믪쓣 ?몄옄 留듭쓽 媛믪쑝濡?移섑솚?⑸땲??
     *
     * @param template 硫붿떆吏 ?쒗뵆由?     * @param arguments 移섑솚??硫붿떆吏 ?몄옄
     * @return 移섑솚??硫붿떆吏. ?몄옄媛 ?녾굅???ㅺ? ?놁쑝硫??먮옒 ?뚮젅?댁뒪??붾? ?좎??⑸땲??
     */
    public static String format(String template, Map<String, Object> arguments) {
        if (template == null || template.isBlank() || arguments == null || arguments.isEmpty()) {
            return template;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer formatted = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = arguments.get(key);
            if (value == null) {
                matcher.appendReplacement(formatted, Matcher.quoteReplacement(matcher.group()));
            } else {
                matcher.appendReplacement(formatted, Matcher.quoteReplacement(String.valueOf(value)));
            }
        }
        matcher.appendTail(formatted);
        return formatted.toString();
    }

    /**
     * Formats indexed placeholders such as {@code {0}}, {@code {1}}.
     *
     * <p>This is the standard CPF message format for DB-managed common messages.
     * The map based formatter remains supported for existing named placeholders.</p>
     */
    public static String format(String template, Object... arguments) {
        if (template == null || template.isBlank() || arguments == null || arguments.length == 0) {
            return template;
        }

        String formatted = template;
        for (int index = 0; index < arguments.length; index++) {
            Object value = arguments[index];
            if (value != null) {
                formatted = formatted.replace("{" + index + "}", String.valueOf(value));
            }
        }
        return formatted;
    }
}

