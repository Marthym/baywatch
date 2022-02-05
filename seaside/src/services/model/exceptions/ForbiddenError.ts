import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";

export class ForbiddenError extends HttpStatusError {
    public httpStatus: number;

    constructor(message: string) {
        super(403, message);
    }
}