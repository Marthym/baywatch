import {Notification} from "@/services/notification/Notification.type";
import NotificationListener from "@/services/notification/NotificationListener";

const DELAY = 3000;

export class NotificationService {
    private notifs: Notification[] = [];
    private listeners: NotificationListener[] = [];
    private timeout?: number;

    /**
     * Push a new {@link Notification} to the stack
     * @param notif The new {@link Notification}
     */
    public pushNotification(notif: Notification): void {
        this.notifs.push(notif);
        if (!this.timeout) {
            this.timeout = setTimeout(NotificationService.onNotificationExpiration, DELAY, this);
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

    public unregisterNotificationListener(listener: NotificationListener): void {
        const idx = this.listeners.indexOf(listener);
        this.listeners.splice(idx, 1);
    }

    private static onNotificationExpiration(ns: NotificationService): void {
        clearTimeout(ns.timeout);
        ns.timeout = undefined;
        const notif = ns.notifs.shift();
        if (notif) {
            if (ns.notifs.length > 0) {
                ns.timeout = setTimeout(NotificationService.onNotificationExpiration, DELAY, ns);
            }
            ns.listeners.forEach(listener => listener.onPopNotification(notif));
        }
    }

    /**
     * Remove immediatly all {@link Notification} and all {@link NotificationListener}
     */
    public destroy(): void {
        this.notifs.forEach(() => {
            NotificationService.onNotificationExpiration(this);
        });
        this.listeners = [];
    }
}

export default new NotificationService();