import { SandSideError } from '@/common/errors/SandSideError';

export class ValidationError extends SandSideError {
    public properties: string[];

    constructor(message: string, properties: string[]) {
        super('UKN0042', message);
        this.properties = properties;
    }
}