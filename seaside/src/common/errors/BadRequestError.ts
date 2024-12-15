import { SandSideError } from '@/common/errors/SandSideError';

export class BadRequestError extends SandSideError {
    constructor(code: string, message: string) {
        super(code, message);
    }
}