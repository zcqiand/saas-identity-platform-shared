// Emit TS api-client (react-query) + zod schemas via orval
import { execSync } from "node:child_process";
import { resolve } from "node:path";
import { mkdirSync, existsSync } from "node:fs";

const root = resolve(import.meta.dirname, "../..");
const openapi = resolve(root, "generated/openapi/openapi.yaml");
if (!existsSync(openapi)) {
  console.error("[emit-ts-client] run emit:openapi first");
  process.exit(1);
}

const outDir = resolve(root, "generated/ts");
for (const sub of ["api-client", "msw"]) {
  mkdirSync(resolve(outDir, sub), { recursive: true });
}

console.log("[emit-ts-client] running orval (api-client + msw + zod)...");
execSync("npx orval", { cwd: root, stdio: "inherit" });
console.log("[emit-ts-client] OK");