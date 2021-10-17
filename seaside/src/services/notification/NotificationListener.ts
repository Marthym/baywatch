import {Notification} from "@/services/notification/Notification";

/**
 * Must be implemented to interact with {@link Notification} evolutions
 */
export default interface NotificationListener {
    /**
     * Executed when new {@link Notification} enter the stack
     * @param notif The new {@link Notification}
     */
    onPushNotification(notif: Notification): void;

    /**
     * Executed when {@link Notification} go out of the stack
     * @param notif The {@link Notification} going out
     */
    onPopNotification(notif: Notification): void;
}