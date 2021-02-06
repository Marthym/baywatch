import {SandSideError} from "@/services/model/exceptions/SandSideError";

export class UnknownFetchError extends SandSideError {

    constructor(message: string) {
        super("UKN0042", message);
    }
}