import {Severity} from "@/services/notification/Severity.enum";
import {NotificationCode} from "@/services/notification/NotificationCode.enum";

export type Notification = {
    code: NotificationCode;
    severity: Severity;
    message: string;
}