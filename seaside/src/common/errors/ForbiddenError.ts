import {HttpStatusError} from "@/common/errors/HttpStatusError";

export class ForbiddenError extends HttpStatusError {
    constructor(message: string) {
        super(403, message);
    }
}