import {NotificationService} from "@/services/notification/NotificationService";
import {NotificationCode} from "@/services/notification/NotificationCode.enum";
import {Severity} from "@/services/notification/Severity.enum";
import NotificationListener from "@/services/notification/NotificationListener";

describe('Test Notification service', () => {
    let tested: NotificationService;

    jest.useFakeTimers();

    beforeEach(() => tested = new NotificationService(500));

    test('should push/pop notifications', () => {
        const actuals: string[] = [];
        const listener: NotificationListener = {
            onPopNotification: n => actuals.push(`pop : ${n.message}`),
            onPushNotification: n => actuals.push(`push : ${n.message}`),
        };
        tested.registerNotificationListener(listener);

        tested.pushNotification({code: NotificationCode.UNAUTHORIZED, severity: Severity.info, message: 'push'});
        tested.pushNotification({code: NotificationCode.UNAUTHORIZED, severity: Severity.info, message: 'push2'});

        expect(actuals).toEqual([
            'push : push', 'push : push2',
        ]);
        actuals.splice(0, 2);

        jest.runOnlyPendingTimers();
        expect(actuals).toEqual([
            'pop : push',
        ]);
        jest.runOnlyPendingTimers();
        expect(actuals).toEqual([
            'pop : push', 'pop : push2',
        ]);
        actuals.splice(0, 2);

        tested.unregisterNotificationListener(listener);

        tested.pushNotification({code: NotificationCode.UNAUTHORIZED, severity: Severity.info, message: 'push3'});

        jest.runOnlyPendingTimers();

        expect(actuals).toEqual([]);
    });

    test('should destroy service', () => {
        const actuals: string[] = [];
        const listener: NotificationListener = {
            onPopNotification: n => actuals.push(`pop : ${n.message}`),
            onPushNotification: n => actuals.push(`push : ${n.message}`),
        };
        tested.registerNotificationListener(listener);
        tested.pushNotification({code: NotificationCode.UNAUTHORIZED, severity: Severity.info, message: 'push'});
        tested.destroy();
        expect(actuals).toEqual([
            'push : push', 'pop : push',
        ]);
    });
});