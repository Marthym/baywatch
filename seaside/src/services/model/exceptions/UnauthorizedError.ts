import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";

export class UnauthorizedError extends HttpStatusError {
    public httpStatus: number;

    constructor(message: string) {
        super(401, message);
    }
}