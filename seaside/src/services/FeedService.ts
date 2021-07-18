import {fromFetch} from "rxjs/fetch";
import {map, switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {Feed} from "@/services/model/Feed";
import {Page} from "@/services/model/Page";
import {from, Observable} from "rxjs";
import {ConstantFilters, ConstantHttpHeaders, ConstantMediaTypes} from "@/constants";

export class FeedService {

    public static readonly DEFAULT_PER_PAGE: number = 20;
    public static readonly DEFAULT_QUERY: string = `?${ConstantFilters.PER_PAGE}=${FeedService.DEFAULT_PER_PAGE}`;

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
    list(page = 0, query: URLSearchParams = new URLSearchParams(FeedService.DEFAULT_QUERY)): Observable<Page<Feed>> {
        const resolvedPage = (page > 0) ? page : 0;
        query.set(ConstantFilters.PAGE, String(resolvedPage));
        let resolvedPerPage = query.get(ConstantFilters.PER_PAGE);
        if (resolvedPerPage === null) {
            resolvedPerPage = String(FeedService.DEFAULT_PER_PAGE);
            query.append(ConstantFilters.PER_PAGE, resolvedPerPage);
        }

        return fromFetch(`${this.serviceBaseUrl}/feeds?${query.toString()}`).pipe(
            map(response => {
                if (response.ok) {
                    const totalCount = parseInt(response.headers.get(ConstantHttpHeaders.X_TOTAL_COUNT) || "-1");
                    const data: Observable<Feed[]> = from(response.json());
                    return {
                        currentPage: resolvedPage,
                        totalPage: Math.ceil(totalCount / Number(resolvedPerPage)),
                        data: data
                    };
                } else {
                    throw new HttpStatusError(response.status, `Error while getting news.`);
                }
            }),
            take(1)
        );
    }

    add(feed: Feed): Observable<Feed> {
        const headers = new Headers();
        headers.set(ConstantHttpHeaders.CONTENT_TYPE, ConstantMediaTypes.JSON_UTF8);
        return fromFetch(`${this.serviceBaseUrl}/feeds`, {
            method: 'POST',
            body: JSON.stringify(feed),
            headers: headers,
        }).pipe(
            switchMap(response => {
                if (response.ok) {
                    return from(response.json());
                } else {
                    throw new HttpStatusError(response.status, `Error while subscribing feed.`);
                }
            }),
            take(1)
        );
    }
}

export default new FeedService(process.env.VUE_APP_API_BASE_URL);