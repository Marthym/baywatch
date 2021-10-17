import {Notification} from "@/services/notification/Notification";
import NotificationListener from "@/services/notification/NotificationListener";

const DELAY: number = 5000;

export class NotificationService {
    private notifs: Notification[] = [];
    private listeners: NotificationListener[] = [];
    private timeout?: number;

    /**
     * Push a new {@link Notification} to the stack
     * @param notif The new {@link Notification}
     */
    public pushNotification(notif: Notification) {
        this.notifs.push(notif);
        if (!this.timeout) {
            this.timeout = setTimeout(this.onNotificationExpiration, DELAY);
        }
        this.listeners.forEach(listener => {
            listener.onPushNotification(notif);
        });
    }

    /**
     * Allow to register a new {@link NotificationListener}
     * @param listener The listener to register
     */
    public registerNotificationListener(listener: NotificationListener): void {
        this.listeners.push(listener);
    }

    private onNotificationExpiration(): void {
        clearTimeout(this.timeout);
        const notif = this.notifs.shift();
        if (notif) {
            if (this.notifs.length > 0) {
                this.timeout = setTimeout(this.onNotificationExpiration, DELAY);
            }
            this.listeners.forEach(listener => listener.onPopNotification(notif));
        }
    }

    /**
     * Remove immediatly all {@link Notification} and all {@link NotificationListener}
     */
    public destroy(): void {
        this.notifs.forEach(n => {
            this.onNotificationExpiration(n);
        });
        this.listeners = [];
    }
}

export default new NotificationService();