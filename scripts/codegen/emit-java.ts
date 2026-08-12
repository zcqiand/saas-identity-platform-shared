// Emit Java Spring POJO + Controller interface via openapi-generator-cli
import { execSync } from "node:child_process";
import { resolve } from "node:path";
import { mkdirSync, existsSync, writeFileSync } from "node:fs";

const root = resolve(import.meta.dirname, "../..");
const openapi = resolve(root, "generated/openapi/openapi.yaml");
if (!existsSync(openapi)) {
  console.error("[emit-java] run emit:openapi first");
  process.exit(1);
}

const outDir = resolve(root, "generated/java");
mkdirSync(outDir, { recursive: true });

const configPath = resolve(root, ".openapi-generator/java-config.json");
const config = {
  library: "spring-boot",
  modelPackage: "saas.identity.shared.dto",
  apiPackage: "saas.identity.shared.api",
  invokerPackage: "saas.identity.shared",
  useTags: true,
  interfaceOnly: true,
  skipDefaultInterface: true,
  useBeanValidation: true,
  useSpringBoot3: true,
  dateLibrary: "java8",
};
mkdirSync(resolve(root, ".openapi-generator"), { recursive: true });
writeFileSync(configPath, JSON.stringify(config, null, 2));

console.log("[emit-java] running openapi-generator-cli -g spring...");
const args = [
  "npx",
  "--no",
  "@openapitools/openapi-generator-cli",
  "generate",
  "-i",
  openapi,
  "-g",
  "spring",
  "-o",
  outDir,
  "--config",
  configPath,
];
execSync(args.join(" "), { cwd: root, stdio: "inherit" });
console.log("[emit-java] OK → generated/java/");