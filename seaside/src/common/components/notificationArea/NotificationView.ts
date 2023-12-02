import { Notification } from '@/services/notification/Notification.type';

export type NotificationView = {
    id: number;
    icon: string;
    raw: Notification;
    doneActions?: string;
}