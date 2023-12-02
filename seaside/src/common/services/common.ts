import {Observable, of, throwError} from "rxjs";
import notificationService from "@/services/notification/NotificationService";
import {NotificationCode} from "@/services/notification/NotificationCode.enum";
import {Severity} from "@/services/notification/Severity.enum";
import {UnauthorizedError} from "@/common/errors/UnauthorizedError";
import {ForbiddenError} from "@/common/errors/ForbiddenError";

export function handleStatusCodeErrors(response: Response): Observable<Response> {
    if (response.ok) {
        of(response);

    } else if (response.status === 401) {
        notificationService.pushNotification({
            code: NotificationCode.UNAUTHORIZED,
            severity: Severity.error,
            message: 'You are not login on !'
        });
        return throwError(() => new UnauthorizedError('You are not login on !'));

    } else if (response.status === 403) {
        notificationService.pushNotification({
            code: NotificationCode.UNAUTHORIZED,
            severity: Severity.error,
            message: 'You are not login on !'
        });
        return throwError(() => new ForbiddenError('You are not allowed for that !'));
    }

    return of(response);
}
