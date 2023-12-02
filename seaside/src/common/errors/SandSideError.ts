export class SandSideError {
    public error: boolean;
    public code: string;
    public message: string;

    constructor(code: string, message: string) {
        this.error = true;
        this.code = code;
        this.message = message;
    }
}