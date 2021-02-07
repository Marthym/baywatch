import {Observable} from 'rxjs';
import {fromFetch} from "rxjs/fetch";
import {switchMap} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";

export default class NewsService {
    /**
     * By default query the 10 latest published news
     * @private
     */
    private static readonly DEFAULT_QUERY: URLSearchParams = new URLSearchParams("?_pp=10&_s=publication");

    serviceBaseUrl: string;

    constructor(baseURL: string) {
        this.serviceBaseUrl = baseURL;
    }

    /**
     * Get the {@link News} from backend
     *
     * @param query
     */
    getNews(query: URLSearchParams = NewsService.DEFAULT_QUERY): Observable<Array<News>> {
        return fromFetch(`${this.serviceBaseUrl}/news?${query.toString()}`).pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new HttpStatusError(response.status, `Error while getting news.`);
                }
            })
        );
    }
}