import {Severity} from "@/services/notification/Severity.enum";

export type Notification = {
    severity: Severity;
    message: string;
}