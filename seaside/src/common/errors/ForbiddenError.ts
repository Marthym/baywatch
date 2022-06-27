import {HttpStatusError} from "@/common/errors/HttpStatusError";

export class ForbiddenError extends HttpStatusError {
    public httpStatus: number;

    constructor(message: string) {
        super(403, message);
    }
}