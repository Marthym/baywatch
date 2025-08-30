export class SandSideError extends Error {
    public error: boolean;
    public code: string;

    constructor(code: string, message: string) {
        super(message);
        this.error = true;
        this.code = code;
    }
}