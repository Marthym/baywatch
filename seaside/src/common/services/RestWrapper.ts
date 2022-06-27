import {Observable} from "rxjs";
import {fromFetch} from "rxjs/fetch";
import {ConstantHttpHeaders, ConstantMediaTypes} from "@/constants";
import {switchMap} from "rxjs/operators";
import {OpPatch} from "json-patch";
import {handleStatusCodeErrors as handleAuthenticationErrors} from "@/common/services/common";

export class RestWrapper {
    private readonly baseUrl: string;

    constructor(baseUrl: string) {
        this.baseUrl = baseUrl;
    }

    get(uri: string): Observable<Response> {
        return fromFetch(this.baseUrl + uri).pipe(
            switchMap(handleAuthenticationErrors),
        );
    }

    post(uri: string, data?: unknown): Observable<Response> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(this.baseUrl + uri, {
            method: 'POST',
            ...RestWrapper.bodyHandler(data),
        }).pipe(
            switchMap(handleAuthenticationErrors),
        );
    }

    put(uri: string, data?: unknown): Observable<Response> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(this.baseUrl + uri, {
            method: 'PUT',
            ...RestWrapper.bodyHandler(data),
        }).pipe(
            switchMap(handleAuthenticationErrors),
        );
    }

    patch(uri: string, data: OpPatch[]): Observable<Response> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_PATCH_UTF8);
        return fromFetch(this.baseUrl + uri, {
            method: 'PATCH',
            ...RestWrapper.bodyHandler(data),
        }).pipe(
            switchMap(handleAuthenticationErrors),
        );
    }

    delete(uri: string, data?: unknown): Observable<Response> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(this.baseUrl + uri, {
            method: 'DELETE',
            ...RestWrapper.bodyHandler(data),
        }).pipe(
            switchMap(handleAuthenticationErrors),
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

}

export default new RestWrapper(import.meta.env.VITE_API_BASE_URL);