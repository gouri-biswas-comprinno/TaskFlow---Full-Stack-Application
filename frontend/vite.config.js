import { defineConfig } from "vitest/config";
import { loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");

  return {
    plugins: [react()],
    test: {
      environment: "jsdom",
      setupFiles: "./src/test-setup.js",
      restoreMocks: true,
    },
    server: {
      host: "0.0.0.0",
      proxy: {
        "/api": {
          target: env.VITE_BACKEND_TARGET || "http://localhost:8080",
        },
      },
    },
  };
});
