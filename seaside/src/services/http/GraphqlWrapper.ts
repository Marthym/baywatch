import {Observable, of, throwError} from "rxjs";
import {fromFetch} from "rxjs/fetch";
import {ConstantHttpHeaders, ConstantMediaTypes} from "@/constants";

import notificationService from '@/services/notification/NotificationService';
import {Severity} from "@/services/notification/Severity.enum";
import {NotificationCode} from "@/services/notification/NotificationCode.enum";
import {map, switchMap} from "rxjs/operators";
import {UnauthorizedError} from "@/services/model/exceptions/UnauthorizedError";
import {ForbiddenError} from "@/services/model/exceptions/ForbiddenError";
import {UnknownFetchError} from "@/services/model/exceptions/UnknownFetchError";
import {GraphqlResponse, INVALID_SYNTAX, UNAUTHORIZED} from "@/services/http/GraphqlResponse.type";

export class GraphqlWrapper {
    private readonly baseUrl: string;

    constructor(baseUrl: string, endpoint: string) {
        this.baseUrl = baseUrl + endpoint;
    }

    send(query: string): Observable<GraphqlResponse> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(this.baseUrl, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                query: GraphqlWrapper.gqlMinify(query)
            })
        }).pipe(
            switchMap(GraphqlWrapper.handleStatusCodeErrors),
            switchMap(r => r.json()),
            map(GraphqlWrapper.handleAuthenticationErrors),
            map(GraphqlWrapper.handleSyntaxErrors)
        );
    }

    /**
     * Minify a GraphQL request body
     * @param gql   The request body
     * @private Minified request body
     */
    private static gqlMinify(gql: string): string {
        return gql
            // replace multiple whitespace with a single
            .replace(/(\b|\B)\s+(\b|\B)/gm, ' ')
            // remove all whitespace between everything except for word and word boundaries
            .replace(/(\B)\s+(\B)|(\b)\s+(\B)|(\B)\s+(\b)/gm, '')
            .trim();
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

    private static handleStatusCodeErrors(response: Response): Observable<Response> {
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

    private static handleAuthenticationErrors(data: GraphqlResponse): GraphqlResponse {
        if (data.errors
            && data.errors.findIndex(e => e.extensions.classification === UNAUTHORIZED) !== -1) {
            notificationService.pushNotification({
                code: NotificationCode.UNAUTHORIZED,
                severity: Severity.error,
                message: 'You are not login on !'
            });
            throw new UnauthorizedError('You are not login on !');
        } else {
            return data;
        }
    }

    private static handleSyntaxErrors(data: GraphqlResponse): GraphqlResponse {
        if (data.errors
            && data.errors.findIndex(e => e.extensions.classification === INVALID_SYNTAX) !== -1) {
            notificationService.pushNotification({
                code: NotificationCode.ERROR,
                severity: Severity.error,
                message: 'Application fail to fetch server !'
            });
            throw new UnknownFetchError('Application fail to fetch server !');
        } else {
            return data;
        }
    }
}

export default new GraphqlWrapper(import.meta.env.VITE_API_BASE_URL, import.meta.env.VITE_GQL_ENDPOINT);