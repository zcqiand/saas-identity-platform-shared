// Snapshot test: lock ASP.NET Core codegen output
import { describe, it, expect } from "vitest";
import { readFileSync, existsSync } from "node:fs";
import { resolve } from "node:path";

const ROOT = resolve(import.meta.dirname, "../../generated/csharp/src/Org.OpenAPITools");

describe("ASP.NET Core codegen snapshot", () => {
  it("emits Controllers under Controllers/", () => {
    const apiDir = resolve(ROOT, "Controllers");
    expect(existsSync(apiDir)).toBe(true);
  });

  it("emits Models under Models/", () => {
    const modelDir = resolve(ROOT, "Models");
    expect(existsSync(modelDir)).toBe(true);
    // DTOs use PascalCase + C# naming convention
    for (const d of ["Tenant", "User", "Role", "OAuthApp"]) {
      expect(existsSync(resolve(modelDir, `${d}.cs`))).toBe(true);
    }
  });

  it("Program.cs exists for runtime", () => {
    expect(existsSync(resolve(ROOT, "Program.cs"))).toBe(true);
  });

  it("appsettings.json exists", () => {
    expect(existsSync(resolve(ROOT, "appsettings.json"))).toBe(true);
  });
});