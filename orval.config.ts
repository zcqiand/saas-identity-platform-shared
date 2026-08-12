import { defineConfig } from "orval";

// orval config — produces TS types + api-client + zod schemas.
// MSW handlers are NOT generated here; they live in saas-identity-platform-msw
// to keep this shared仓 free of faker/msw devDeps (lighter install + cleaner
// concern separation: shared = contract, msw = mock behavior).
export default defineConfig({
  // React Query api-client (real fetch — no mock)
  saas: {
    input: "./generated/openapi/openapi.yaml",
    output: {
      mode: "split",
      target: "./generated/ts/api-client/endpoints.ts",
      client: "react-query",
      override: {
        useDates: false,
        query: { useQuery: true, useInfinite: false, useSuspenseQuery: false, signal: true },
      },
    },
  },

  // Zod schemas (运行时校验，替代手写 zod)
  saasZod: {
    input: "./generated/openapi/openapi.yaml",
    output: {
      mode: "split",
      target: "./generated/ts/zod-schemas.ts",
      client: "zod",
    },
  },
});