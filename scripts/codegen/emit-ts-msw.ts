// Placeholder: emit-ts-msw is now part of emit-ts-client (orval runs all 3 targets at once)
// This script exists so the npm script alias works for symmetry.
import { execSync } from "node:child_process";
import { resolve } from "node:path";

const root = resolve(import.meta.dirname, "../..");
console.log("[emit-ts-msw] delegated to emit-ts-client (orval runs all targets)...");
execSync("tsx scripts/codegen/emit-ts-client.ts", { cwd: root, stdio: "inherit" });
console.log("[emit-ts-msw] OK");