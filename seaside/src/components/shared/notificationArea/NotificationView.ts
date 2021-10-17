import {Severity} from "@/services/notification/Severity.enum";

export type NotificationView = {
    severity: Severity;
    message: string;
}