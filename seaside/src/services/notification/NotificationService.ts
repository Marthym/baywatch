import {Notification} from "@/services/notification/Notification";
import NotificationListener from "@/services/notification/NotificationListener";

export class NotificationService {
    private notifs: Notification[];
    private listener: NotificationListener[];

    /**
     * Push a new {@link Notification} to the stack
     * @param notif The new {@link Notification}
     */
    public pushNotification(notif: Notification) {

    }

    /**
     * Allow to register a new {@link NotificationListener}
     * @param listener The listener to register
     */
    public registerNotificationListener(listener: NotificationListener): void {

    }
}

export default new NotificationService();