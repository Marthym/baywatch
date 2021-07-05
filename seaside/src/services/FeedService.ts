import {fromFetch} from "rxjs/fetch";
import {map, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {Feed} from "@/services/model/Feed";
import {Page} from "@/services/model/Page";
import {from, Observable} from "rxjs";

export class FeedService {

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
    list(page = 1, query: URLSearchParams = new URLSearchParams(FeedService.DEFAULT_QUERY)): Observable<Page<Feed>> {
        if (page > 1) {
            query.append('_p', String(page));
        }
        return fromFetch(`${this.serviceBaseUrl}/feeds?${query.toString()}`).pipe(
            map(response => {
                if (response.ok) {
                    const totalCount = parseInt(response.headers.get('X-Total-Count') || "-1");
                    const data: Observable<Feed[]> = from(response.json());
                    return {totalCount: totalCount, data: data};
                } else {
                    throw new HttpStatusError(response.status, `Error while getting news.`);
                }
            }),
            take(1)
        );
    }
}

export default new FeedService(process.env.VUE_APP_API_BASE_URL);