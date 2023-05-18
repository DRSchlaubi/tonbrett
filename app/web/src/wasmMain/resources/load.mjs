import { instantiate } from './tonbrett-web-wasm.uninstantiated.mjs';

await wasmSetup;
instantiate({ skia: Module['asm'] });
