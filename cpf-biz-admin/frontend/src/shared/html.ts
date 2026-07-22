export function displayValue(input: unknown): string {
  return input === null || input === undefined || input === "" ? "-" : String(input);
}

export function escapeHtml(input: unknown): string {
  const replacements: Record<string, string> = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "'": "&#39;",
    "\"": "&quot;"
  };
  return displayValue(input).replace(/[&<>'"]/g, character => replacements[character]);
}

export function escapeAttribute(input: unknown): string {
  return escapeHtml(input ?? "");
}
