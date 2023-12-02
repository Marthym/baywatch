import {SandSideError} from "@/common/errors/SandSideError";

export class UnknownFetchError extends SandSideError {

    constructor(message: string) {
        super("UKN0042", message);
    }
}