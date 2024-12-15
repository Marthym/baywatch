import { defineConfig, mergeConfig } from 'vitest/config';
import viteConfig from './vite.config';

export default defineConfig(configEnv => mergeConfig(
    viteConfig(configEnv),
    defineConfig({
        test: {
            global: true,
            environment: 'jsdom',
            coverage: {
                reporter: ['text', 'lcov'],
            },
        },
    }),
));
