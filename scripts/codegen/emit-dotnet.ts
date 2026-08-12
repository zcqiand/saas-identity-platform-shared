// Emit ASP.NET Core DTO + Controller base via openapi-generator-cli
import { execSync } from "node:child_process";
import { resolve } from "node:path";
import { mkdirSync, existsSync, writeFileSync } from "node:fs";

const root = resolve(import.meta.dirname, "../..");
const openapi = resolve(root, "generated/openapi/openapi.yaml");
if (!existsSync(openapi)) {
  console.error("[emit-dotnet] run emit:openapi first");
  process.exit(1);
}

const outDir = resolve(root, "generated/csharp");
mkdirSync(outDir, { recursive: true });

const configPath = resolve(root, ".openapi-generator/dotnet-config.json");
const config = {
  targetFramework: "net8.0",
  useSwashbuckle: true,
  modelNamespace: "Saas.Identity.Shared.Dto",
  apiNamespace: "Saas.Identity.Shared.Api",
  generateAliasesAsModels: true,
  nullableReferences: true,
  operationIsAbstract: true,
};
mkdirSync(resolve(root, ".openapi-generator"), { recursive: true });
writeFileSync(configPath, JSON.stringify(config, null, 2));

console.log("[emit-dotnet] running openapi-generator-cli -g aspnetcore...");
const args = [
  "npx",
  "--no",
  "@openapitools/openapi-generator-cli",
  "generate",
  "-i",
  openapi,
  "-g",
  "aspnetcore",
  "-o",
  outDir,
  "--config",
  configPath,
];
execSync(args.join(" "), { cwd: root, stdio: "inherit" });
console.log("[emit-dotnet] OK → generated/csharp/");