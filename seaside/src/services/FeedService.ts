import {Observable} from "rxjs";
import {fromFetch} from "rxjs/fetch";
import {switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {Feed} from "@/services/model/Feed";

export default class FeedService {

    public static readonly DEFAULT_QUERY: string = '?_pp=20';

    serviceBaseUrl: string;

    constructor(baseURL: string) {
        this.serviceBaseUrl = baseURL;
    }

    /**
     * Get the {@link Feed} from backend
     *
     * @param page The to display
     * @param query The possible query parameters
     */
    list(page = 1, query: URLSearchParams = new URLSearchParams(FeedService.DEFAULT_QUERY)): Observable<Feed[]> {
        if (page > 1) {
            query.append('_p', String(page));
        }
        return fromFetch(`${this.serviceBaseUrl}/feeds?${query.toString()}`).pipe(
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
}