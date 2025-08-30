import { SandSideError } from '@/common/errors/SandSideError';

export class UnauthorizedError extends SandSideError {
    constructor(code: string, message: string) {
        super(code, message);
    }
}