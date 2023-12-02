import {SandSideError} from "@/common/errors/SandSideError";

export class HttpStatusError extends SandSideError {
    public httpStatus: number;

    constructor(httpStatus: number, message: string) {
        super(`SSHTTP${httpStatus}`, message);
        this.httpStatus = httpStatus;
    }
}