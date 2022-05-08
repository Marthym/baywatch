interface ImportMetaEnv {
    readonly VITE_API_BASE_URL: string
    readonly VITE_BW_VERSION: string
    // more env variables...
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}