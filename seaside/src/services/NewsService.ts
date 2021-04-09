import {Observable} from 'rxjs';
import {fromFetch} from "rxjs/fetch";
import {switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {News} from "@/services/model/News";
import {Mark} from "@/services/model/Mark.enum";

export default class NewsService {
    /**
     * By default query the 10 latest published news
     * @private
     */
    public static readonly DEFAULT_QUERY: string = '?_pp=20&_s=-publication';

    serviceBaseUrl: string;

    constructor(baseURL: string) {
        this.serviceBaseUrl = baseURL;
    }

    /**
     * Get the {@link News} from backend
     *
     * @param page The to display
     * @param query The possible query parameters
     */
    getNews(page = 1, query: URLSearchParams = new URLSearchParams(NewsService.DEFAULT_QUERY)): Observable<News[]> {
        if (page > 1) {
            query.append('_p', String(page));
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

    mark(id: string, mark: Mark): Observable<News> {
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

    unmark(id: string, mark: Mark): Observable<News> {
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