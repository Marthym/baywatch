import {Severity} from "@/services/notification/Severity.enum";
import {NotificationCode} from "@/services/notification/NotificationCode.enum";

export type Notification = {
    code: NotificationCode;
    severity: Severity;
    title?: string;
    message: string;
    delay?: number;
    actions?: string;
    target?: string;
}