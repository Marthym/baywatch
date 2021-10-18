import {map, Observable} from "rxjs";
import {fromFetch} from "rxjs/fetch";
import {ConstantHttpHeaders, ConstantMediaTypes} from "@/constants";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";

import notificationService from '@/services/notification/NotificationService';
import {Severity} from "@/services/notification/Severity.enum";


export class RestWrapper {
    private readonly baseUrl: string;

    constructor(baseUrl: string) {
        this.baseUrl = baseUrl;
    }

    get(uri: string): Observable<Response> {
        return fromFetch(this.baseUrl + uri);
    }

    post(uri: string, data?: unknown): Observable<Response> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(this.baseUrl + uri, {
            method: 'POST',
            ...RestWrapper.bodyHandler(data),
        });
    }

    put(uri: string, data?: unknown): Observable<Response> {
        console.info('wrapped put...')
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(this.baseUrl + uri, {
            method: 'PUT',
            ...RestWrapper.bodyHandler(data),
        }).pipe(
            map(response => {
                if (response.ok) {
                    return response;
                } else {
                    notificationService.pushNotification({severity: Severity.error, message: 'Unauthorized'});
                    throw new HttpStatusError(response.status, 'Error while updating feed.');
                }
            }),
        );
    }

    delete(uri: string, data?: unknown): Observable<Response> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(this.baseUrl + uri, {
            method: 'DELETE',
            ...RestWrapper.bodyHandler(data),
        });
    }

    private static bodyHandler(data?: unknown): { body?: BodyInit, headers?: HeadersInit } {
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
}

export default new RestWrapper(process.env.VUE_APP_API_BASE_URL);