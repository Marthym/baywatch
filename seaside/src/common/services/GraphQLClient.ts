import {Observable} from "rxjs";
import {fromFetch} from "rxjs/fetch";
import {ConstantHttpHeaders, ConstantMediaTypes} from "@/constants";
import {map, switchMap} from "rxjs/operators";
import {UnauthorizedError} from "@/common/errors/UnauthorizedError";
import {UnknownFetchError} from "@/common/errors/UnknownFetchError";
import {
    FORBIDDEN,
    GraphqlResponse,
    INTERNAL_ERROR,
    INVALID_SYNTAX,
    UNAUTHORIZED,
    VALIDATION_ERROR
} from "@/common/model/GraphqlResponse.type";
import {handleStatusCodeErrors} from "@/common/services/common";
import {ForbiddenError} from "@/common/errors/ForbiddenError";
import { ValidationError } from '@/common/errors/ValidationError';

const baseUrl = import.meta.env.VITE_API_BASE_URL + import.meta.env.VITE_GQL_ENDPOINT;

/**
 * Minify a GraphQL request body
 * @param gql   The request body
 * @private Minified request body
 */
function gqlMinify(gql: string): string {
    return gql
        .replace('#graphql', '')
        // replace multiple whitespace with a single
        .replace(/(\b|\B)\s+(\b|\B)/gm, ' ')
        // remove all whitespace between everything except for word and word boundaries
        .replace(/(\B)\s+(\B)|(\b)\s+(\B)|(\B)\s+(\b)/gm, '')
        .trim();
}

function handleAuthenticationErrors<T>(data: GraphqlResponse<T>): GraphqlResponse<T> {
    if (data.errors
        && data.errors.findIndex(e => e.extensions.classification === UNAUTHORIZED) !== -1) {
        throw new UnauthorizedError('You are not login on !');
    } else {
        return data;
    }
}

function handleSyntaxErrors<T>(data: GraphqlResponse<T>): GraphqlResponse<T> {
    if (data.errors
        && data.errors.findIndex(e => e.extensions.classification === INVALID_SYNTAX) !== -1) {
        throw new UnknownFetchError('Application fail to fetch server !');
    } else {
        return data;
    }
}

function handleValidationErrors<T>(data: GraphqlResponse<T>): GraphqlResponse<T> {
    if (data.errors) {
        const errIdx = data.errors.findIndex(e => e.extensions.classification === VALIDATION_ERROR);
        if (errIdx !== -1) {
            throw new ValidationError(data.errors[errIdx].message, data.errors[errIdx].extensions.properties);
        }
    }
    return data;
}

function handleInternalServerErrors<T>(data: GraphqlResponse<T>): GraphqlResponse<T> {
    if (data.errors
        && data.errors.findIndex(e => e.extensions.classification === INTERNAL_ERROR) !== -1) {
        throw new UnknownFetchError('An error occurred on the server side !');
    } else {
        return data;
    }
}

function handleForbiddenErrors<T>(data: GraphqlResponse<T>): GraphqlResponse<T> {
    if (data.errors) {
        const errIdx = data.errors.findIndex(e => e.extensions.classification === FORBIDDEN);
        if (errIdx !== -1) {
            throw new ForbiddenError(data.errors[errIdx].message);
        }
    }
    return data;
}

export function send<T>(query: string, vars?: any): Observable<GraphqlResponse<T>> {
    const headers = new Headers();
    headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
    return fromFetch(baseUrl, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({
            query: gqlMinify(query),
            variables: vars
        })
    }).pipe(
        switchMap(handleStatusCodeErrors),
        switchMap(r => r.json() as Promise<GraphqlResponse<T>>),
        map(handleAuthenticationErrors),
        map(handleForbiddenErrors),
        map(handleValidationErrors),
        map(handleSyntaxErrors),
        map(handleInternalServerErrors),
    );
}
