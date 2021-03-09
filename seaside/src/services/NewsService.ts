import {Observable} from 'rxjs';
import {fromFetch} from "rxjs/fetch";
import {switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {News} from "@/services/model/News";

export default class NewsService {
    /**
     * By default query the 10 latest published news
     * @private
     */
    private static readonly DEFAULT_QUERY: URLSearchParams = new URLSearchParams("?_pp=50&_s=-publication");

    serviceBaseUrl: string;

    constructor(baseURL: string) {
        this.serviceBaseUrl = baseURL;
    }

    /**
     * Get the {@link News} from backend
     *
     * @param page
     * @param query
     */
    getNews(page = 1, query: URLSearchParams = NewsService.DEFAULT_QUERY): Observable<News[]> {
        if (page > 1) {
            query.append('-p', String(page));
        }
        return fromFetch(`${this.serviceBaseUrl}/news?${query.toString()}`)
            .pipe(
                switchMap(response => {
                    if (response.ok) {
                        return response.json();
                    } else {
                        throw new HttpStatusError(response.status, `Error while getting news.`);
                    }
                }),
                take(1)
            );
    }

    mark(id: string, mark: string): Observable<News> {
        return fromFetch(`${this.serviceBaseUrl}/news/${id}/mark/${mark}`, {method: 'PUT'})
            .pipe(
                switchMap(response => {
                    if (response.ok) {
                        return response.json();
                    } else {
                        throw new HttpStatusError(response.status, `Error while mark news ${id} as ${mark}.`);
                    }
                }),
                take(1)
            );
    }

    unmark(id: string, mark: string): Observable<News> {
        return fromFetch(`${this.serviceBaseUrl}/news/${id}/unmark/${mark}`, {method: 'PUT'})
            .pipe(
                switchMap(response => {
                    if (response.ok) {
                        return response.json();
                    } else {
                        throw new HttpStatusError(response.status, `Error while unmark news ${id} as ${mark}.`);
                    }
                }),
                take(1)
            );
    }
}