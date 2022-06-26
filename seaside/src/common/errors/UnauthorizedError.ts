import {HttpStatusError} from "@/common/errors/HttpStatusError";

export class UnauthorizedError extends HttpStatusError {
    public httpStatus: number;

    constructor(message: string) {
        super(401, message);
    }
}