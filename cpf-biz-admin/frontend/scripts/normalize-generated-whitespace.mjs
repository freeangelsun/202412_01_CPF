import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const generatedRoot = path.resolve(root, process.env.CPF_GENERATED_ROOT || "src/generated");
const checkOnly = process.argv.includes("--check");
const textExtensions = new Set([".ts", ".tsx", ".js", ".mjs", ".json"]);
const changed = [];
const violations = [];

function normalize(text) {
  const normalized = text.replace(/\r\n/g, "\n").split("\n").map(line => line.replace(/[ \t]+$/g, "")).join("\n");
  return normalized.replace(/\n*$/, "\n");
}
function visit(directory) {
  if (!fs.existsSync(directory)) return;
  for (const entry of fs.readdirSync(directory, { withFileTypes: true }).sort((a, b) => a.name.localeCompare(b.name))) {
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) visit(absolute);
    else if (entry.isFile() && textExtensions.has(path.extname(entry.name))) {
      const original = fs.readFileSync(absolute, "utf8");
      const normalized = normalize(original);
      if (normalized !== original) {
        const relative = path.relative(root, absolute).split(path.sep).join("/");
        violations.push(relative);
        if (!checkOnly) { fs.writeFileSync(absolute, normalized); changed.push(relative); }
      }
    }
  }
}
visit(generatedRoot);
if (checkOnly && violations.length) {
  throw new Error(`Generated whitespace drift files=${violations.length}: ${violations.slice(0, 20).join(", ")}`);
}
console.log(`[CPF][FRONTEND][PASS] generated whitespace ${checkOnly ? "check" : "normalize"} files=${checkOnly ? 0 : changed.length}`);
