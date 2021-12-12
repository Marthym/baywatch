import {Notification} from "@/services/notification/Notification.type";
import NotificationListener from "@/services/notification/NotificationListener";
import {NotificationCode} from "@/services/notification/NotificationCode.enum";
import {Severity} from "@/services/notification/Severity.enum";

const DELAY = 3000;

export class NotificationService {
    private readonly delay: number;
    private notifs: Notification[] = [];
    private listeners: NotificationListener[] = [];
    private timeout?: number;


    constructor(delay: number = DELAY) {
        this.delay = delay;
    }

    /**
     * Push a new {@link Notification} to the stack
     * @param notif The new {@link Notification}
     */
    public pushNotification(notif: Notification): void {
        this.notifs.push(notif);
        if (!this.timeout) {
            this.timeout = window.setTimeout(NotificationService.onNotificationExpiration, this.delay, this);
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
        window.clearTimeout(ns.timeout);
        ns.timeout = undefined;
        const notif = ns.notifs.shift();
        if (notif) {
            if (ns.notifs.length > 0) {
                ns.timeout = window.setTimeout(NotificationService.onNotificationExpiration, ns.delay, ns);
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

    public pushSimpleOk(message: string): void {
        this.pushNotification({
            code: NotificationCode.OK,
            severity: Severity.info,
            message: message,
        })
    }

    public pushSimpleError(message: string): void {
        this.pushNotification({
            code: NotificationCode.ERROR,
            severity: Severity.error,
            message: message,
        })
    }
}

export default new NotificationService();