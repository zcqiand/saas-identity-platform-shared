import { defineConfig } from "orval";

export default defineConfig({
  // React Query api-client + MSW mock handlers (split mode)
  saas: {
    input: "./generated/openapi/openapi.yaml",
    output: {
      mode: "split",
      target: "./generated/ts/api-client/endpoints.ts",
      client: "react-query",
      mock: {
        type: "msw",
        generateEachHttpStatus: false,
        baseURL: "http://localhost:5173",
      },
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