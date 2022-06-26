import {Observable, of, throwError} from "rxjs";
import {fromFetch} from "rxjs/fetch";
import {ConstantHttpHeaders, ConstantMediaTypes} from "@/constants";

import notificationService from '@/services/notification/NotificationService';
import {Severity} from "@/services/notification/Severity.enum";
import {NotificationCode} from "@/services/notification/NotificationCode.enum";
import {map, switchMap} from "rxjs/operators";
import {UnauthorizedError} from "@/common/errors/UnauthorizedError";
import {ForbiddenError} from "@/common/errors/ForbiddenError";
import {UnknownFetchError} from "@/common/errors/UnknownFetchError";
import {GraphqlResponse, INTERNAL_ERROR, INVALID_SYNTAX, UNAUTHORIZED} from "@/common/model/GraphqlResponse.type";

export class GraphqlWrapper {
    private readonly baseUrl: string;

    constructor(baseUrl: string, endpoint: string) {
        this.baseUrl = baseUrl + endpoint;
    }

    send<T>(query: string, vars?: any): Observable<GraphqlResponse<T>> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(this.baseUrl, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                query: GraphqlWrapper.gqlMinify(query),
                variables: vars
            })
        }).pipe(
            switchMap(GraphqlWrapper.handleStatusCodeErrors),
            switchMap(r => r.json()),
            map(GraphqlWrapper.handleAuthenticationErrors),
            map(GraphqlWrapper.handleSyntaxErrors),
            map(GraphqlWrapper.handleInternalServerErrors),
        );
    }

    /**
     * Minify a GraphQL request body
     * @param gql   The request body
     * @private Minified request body
     */
    private static gqlMinify(gql: string): string {
        return gql
            .replace('#graphql', '')
            // replace multiple whitespace with a single
            .replace(/(\b|\B)\s+(\b|\B)/gm, ' ')
            // remove all whitespace between everything except for word and word boundaries
            .replace(/(\B)\s+(\B)|(\b)\s+(\B)|(\B)\s+(\b)/gm, '')
            .trim();
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

    private static handleAuthenticationErrors<T>(data: GraphqlResponse<T>): GraphqlResponse<T> {
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

    private static handleSyntaxErrors<T>(data: GraphqlResponse<T>): GraphqlResponse<T> {
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

    private static handleInternalServerErrors<T>(data: GraphqlResponse<T>): GraphqlResponse<T> {
        if (data.errors
            && data.errors.findIndex(e => e.extensions.classification === INTERNAL_ERROR) !== -1) {
            notificationService.pushNotification({
                code: NotificationCode.ERROR,
                severity: Severity.error,
                message: 'An error occurred on the server side'
            });
            throw new UnknownFetchError('An error occurred on the server side !');
        } else {
            return data;
        }
    }
}

export default new GraphqlWrapper(import.meta.env.VITE_API_BASE_URL, import.meta.env.VITE_GQL_ENDPOINT);