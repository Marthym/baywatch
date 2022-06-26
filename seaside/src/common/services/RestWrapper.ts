import {Observable, of, throwError} from "rxjs";
import {fromFetch} from "rxjs/fetch";
import {ConstantHttpHeaders, ConstantMediaTypes} from "@/constants";

import notificationService from '@/services/notification/NotificationService';
import {Severity} from "@/services/notification/Severity.enum";
import {NotificationCode} from "@/services/notification/NotificationCode.enum";
import {switchMap} from "rxjs/operators";
import {OpPatch} from "json-patch";
import {UnauthorizedError} from "@/common/errors/UnauthorizedError";
import {ForbiddenError} from "@/common/errors/ForbiddenError";


export class RestWrapper {
    private readonly baseUrl: string;

    constructor(baseUrl: string) {
        this.baseUrl = baseUrl;
    }

    get(uri: string): Observable<Response> {
        return fromFetch(this.baseUrl + uri).pipe(
            switchMap(RestWrapper.handleAuthenticationErrors),
        );
    }

    post(uri: string, data?: unknown): Observable<Response> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(this.baseUrl + uri, {
            method: 'POST',
            ...RestWrapper.bodyHandler(data),
        }).pipe(
            switchMap(RestWrapper.handleAuthenticationErrors),
        );
    }

    put(uri: string, data?: unknown): Observable<Response> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(this.baseUrl + uri, {
            method: 'PUT',
            ...RestWrapper.bodyHandler(data),
        }).pipe(
            switchMap(RestWrapper.handleAuthenticationErrors),
        );
    }

    patch(uri: string, data: OpPatch[]): Observable<Response> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_PATCH_UTF8);
        return fromFetch(this.baseUrl + uri, {
            method: 'PATCH',
            ...RestWrapper.bodyHandler(data),
        }).pipe(
            switchMap(RestWrapper.handleAuthenticationErrors),
        );
    }

    delete(uri: string, data?: unknown): Observable<Response> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(this.baseUrl + uri, {
            method: 'DELETE',
            ...RestWrapper.bodyHandler(data),
        }).pipe(
            switchMap(RestWrapper.handleAuthenticationErrors),
        );
    }

    private static bodyHandler(data?: unknown): { body?: BodyInit | null, headers?: HeadersInit } {
        if (data instanceof FormData) {
            return {
                body: data,
            };

        } else if (data) {
            const headers = new Headers();
            headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
            return {
                body: JSON.stringify(data),
                headers: headers,
            };

        } else {
            return {};
        }
    }

    private static handleAuthenticationErrors(response: Response): Observable<Response> {
        if (response.ok) {
            return of(response);

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
}

export default new RestWrapper(import.meta.env.VITE_API_BASE_URL as string);