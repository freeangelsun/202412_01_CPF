/** Strict JSON object parser used before approval hashing or submission.
 * Duplicate keys (including NFC collisions) and lossy numeric literals are rejected.
 * High precision numbers must be represented as strings so browser/server canonicalization agrees.
 */
export function parseStrictJsonObject(source: string, label = "JSON"): Record<string, unknown> {
  let index = 0;
  const length = source.length;
  const skip = () => { while (index < length && /\s/u.test(source[index])) index += 1; };
  const fail = (message: string): never => { throw new Error(`${label}: ${message} (offset ${index})`); };
  const parseString = (): string => {
    if (source[index] !== '"') fail("문자열이 필요합니다");
    const start = index++;
    let escaped = false;
    while (index < length) {
      const ch = source[index++];
      if (escaped) { escaped = false; continue; }
      if (ch === "\\") { escaped = true; continue; }
      if (ch === '"') {
        try { return JSON.parse(source.slice(start, index)) as string; }
        catch { fail("잘못된 JSON 문자열입니다"); }
      }
      if (ch.charCodeAt(0) < 0x20) fail("제어문자는 문자열에 사용할 수 없습니다");
    }
    return fail("닫히지 않은 문자열입니다");
  };
  const parseNumber = (): number => {
    const rest = source.slice(index);
    const match = /^-?(?:0|[1-9]\d*)(?:\.\d+)?(?:[eE][+-]?\d+)?/.exec(rest);
    if (!match) return fail("잘못된 숫자입니다");
    const literal = match[0]; index += literal.length;
    const digits = literal.replace(/[-+.eE]/g, "").replace(/^0+/, "");
    if (!/[.eE]/.test(literal)) {
      let integer: bigint;
      try { integer = BigInt(literal); }
      catch { return fail("잘못된 정수입니다"); }
      if (integer > BigInt(Number.MAX_SAFE_INTEGER) || integer < BigInt(Number.MIN_SAFE_INTEGER))
        fail("안전 정수 범위를 넘는 숫자는 문자열로 입력해야 합니다");
    } else if (digits.length > 15) {
      fail("정밀도 손실 가능성이 있는 소수는 문자열로 입력해야 합니다");
    }
    const value = Number(literal);
    if (!Number.isFinite(value)) fail("유한 숫자만 허용됩니다");
    return Object.is(value, -0) ? 0 : value;
  };
  const parseValue = (): unknown => {
    skip();
    const ch = source[index];
    if (ch === '{') return parseObject();
    if (ch === '[') return parseArray();
    if (ch === '"') return parseString().normalize("NFC");
    if (source.startsWith("true", index)) { index += 4; return true; }
    if (source.startsWith("false", index)) { index += 5; return false; }
    if (source.startsWith("null", index)) { index += 4; return null; }
    if (ch === '-' || /\d/.test(ch ?? "")) return parseNumber();
    return fail("값을 해석할 수 없습니다");
  };
  const parseObject = (): Record<string, unknown> => {
    const value: Record<string, unknown> = {}; const keys = new Set<string>(); index += 1; skip();
    if (source[index] === '}') { index += 1; return value; }
    while (index < length) {
      skip(); const key = parseString().normalize("NFC");
      if (keys.has(key)) fail(`중복 키가 있습니다: ${key}`); keys.add(key);
      skip(); if (source[index++] !== ':') fail(":가 필요합니다");
      value[key] = parseValue(); skip();
      const delimiter = source[index++]; if (delimiter === '}') return value;
      if (delimiter !== ',') fail(", 또는 }가 필요합니다");
    }
    return fail("닫히지 않은 객체입니다");
  };
  const parseArray = (): unknown[] => {
    const value: unknown[] = []; index += 1; skip();
    if (source[index] === ']') { index += 1; return value; }
    while (index < length) {
      value.push(parseValue()); skip(); const delimiter = source[index++];
      if (delimiter === ']') return value; if (delimiter !== ',') fail(", 또는 ]가 필요합니다");
    }
    return fail("닫히지 않은 배열입니다");
  };
  skip(); const parsed = parseValue(); skip(); if (index !== length) fail("JSON 뒤에 불필요한 문자가 있습니다");
  if (!parsed || Array.isArray(parsed) || typeof parsed !== "object") fail("JSON 객체여야 합니다");
  return parsed as Record<string, unknown>;
}
