import {Severity} from "@/services/notification/Severity.enum";

export type NotificationView = {
    id: number;
    severity: Severity;
    message: string;
}