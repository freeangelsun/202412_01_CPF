import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const clientPath = path.resolve(root, process.env.CPF_GENERATED_CLIENT || "src/generated/orval/cpf-api.ts");

if (!fs.existsSync(clientPath)) throw new Error(`Generated client missing: ${clientPath}`);

let client = fs.readFileSync(clientPath, "utf8");
let cursor = 0;
let changes = 0;
let operations = 0;

function normalizeType(value) {
  return value.replace(/\s+/g, " ").replace(/\s*([{}:;,])\s*/g, "$1").trim();
}

function findBalancedEnd(source, start, open, close) {
  let depth = 0;
  let quote = "";
  let escaped = false;
  for (let index = start; index < source.length; index += 1) {
    const char = source[index];
    if (quote) {
      if (escaped) escaped = false;
      else if (char === "\\") escaped = true;
      else if (char === quote) quote = "";
      continue;
    }
    if (char === "'" || char === '"' || char === "`") {
      quote = char;
      continue;
    }
    if (char === open) depth += 1;
    else if (char === close) {
      depth -= 1;
      if (depth === 0) return index;
    }
  }
  throw new Error(`Unbalanced ${open}${close} sequence at ${start}`);
}

function captureGenericArgument(source, tokenStart, argumentIndex) {
  const genericStart = source.indexOf("<", tokenStart);
  if (genericStart < 0) throw new Error(`Generic start missing after ${source.slice(tokenStart, tokenStart + 80)}`);
  const genericEnd = findBalancedEnd(source, genericStart, "<", ">");
  const body = source.slice(genericStart + 1, genericEnd);
  let depthAngle = 0;
  let depthBrace = 0;
  let depthParen = 0;
  let depthBracket = 0;
  let quote = "";
  let escaped = false;
  let segmentStart = 0;
  const segments = [];
  for (let index = 0; index < body.length; index += 1) {
    const char = body[index];
    if (quote) {
      if (escaped) escaped = false;
      else if (char === "\\") escaped = true;
      else if (char === quote) quote = "";
      continue;
    }
    if (char === "'" || char === '"' || char === "`") { quote = char; continue; }
    if (char === "<") depthAngle += 1;
    else if (char === ">") depthAngle -= 1;
    else if (char === "{") depthBrace += 1;
    else if (char === "}") depthBrace -= 1;
    else if (char === "(") depthParen += 1;
    else if (char === ")") depthParen -= 1;
    else if (char === "[") depthBracket += 1;
    else if (char === "]") depthBracket -= 1;
    else if (char === "," && depthAngle === 0 && depthBrace === 0 && depthParen === 0 && depthBracket === 0) {
      segments.push({ start: genericStart + 1 + segmentStart, end: genericStart + 1 + index, value: body.slice(segmentStart, index) });
      segmentStart = index + 1;
    }
  }
  segments.push({ start: genericStart + 1 + segmentStart, end: genericEnd, value: body.slice(segmentStart) });
  if (argumentIndex >= segments.length) throw new Error(`Generic argument ${argumentIndex} missing`);
  return segments[argumentIndex];
}

function operationNameAt(block) {
  const match = block.match(/export const get([A-Za-z0-9]+)MutationOptions/);
  if (!match) throw new Error("Mutation options operation name missing");
  return match[1].charAt(0).toLowerCase() + match[1].slice(1);
}

while (true) {
  const start = client.indexOf("export const get", cursor);
  if (start < 0) break;
  const tokenEnd = client.indexOf("MutationOptions", start);
  if (tokenEnd < 0 || tokenEnd - start > 180) {
    cursor = start + 12;
    continue;
  }
  const nextMutation = client.indexOf("\nexport const get", tokenEnd + 1);
  const nextResponse = client.indexOf("\nexport type ", tokenEnd + 1);
  const candidates = [nextMutation, nextResponse].filter(value => value >= 0);
  const end = candidates.length ? Math.min(...candidates) : client.length;
  const block = client.slice(start, end);
  if (!block.includes("MutationFunction<")) {
    cursor = end;
    continue;
  }

  const operationId = operationNameAt(block);
  const mutationFnToken = block.indexOf("MutationFunction<");
  const canonical = captureGenericArgument(block, mutationFnToken, 1);
  const canonicalType = canonical.value.trim();
  const canonicalNormalized = normalizeType(canonicalType);

  const replacements = [];
  const genericTokens = ["UseMutationOptions<", "UseMutationReturnType<"];
  for (const token of genericTokens) {
    let tokenCursor = 0;
    while (true) {
      const tokenStart = block.indexOf(token, tokenCursor);
      if (tokenStart < 0) break;
      const variable = captureGenericArgument(block, tokenStart, 2);
      if (normalizeType(variable.value) !== canonicalNormalized) {
        replacements.push({ start: variable.start, end: variable.end, value: canonicalType });
      }
      tokenCursor = variable.end + 1;
    }
  }

  let normalizedBlock = block;
  for (const replacement of replacements.sort((left, right) => right.start - left.start)) {
    normalizedBlock = normalizedBlock.slice(0, replacement.start) + replacement.value + normalizedBlock.slice(replacement.end);
    changes += 1;
  }

  if (normalizedBlock !== block) {
    client = client.slice(0, start) + normalizedBlock + client.slice(end);
    cursor = start + normalizedBlock.length;
  } else {
    cursor = end;
  }
  operations += 1;
}

fs.writeFileSync(clientPath, client);
console.log(`[CPF][FRONTEND] normalized Orval mutation variable contracts: operations=${operations} replacements=${changes}`);
