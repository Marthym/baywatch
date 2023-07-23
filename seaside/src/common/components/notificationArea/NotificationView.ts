import { Notification } from '@/services/notification/Notification.type';

export type NotificationView = {
    id: number;
    raw: Notification;
    doneActions?: string;
}