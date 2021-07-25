import {map, switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {Feed} from "@/services/model/Feed";
import {Page} from "@/services/model/Page";
import {from, Observable} from "rxjs";
import {ConstantFilters, ConstantHttpHeaders} from "@/constants";
import rest from '@/services/http/RestWrapper';

export class FeedService {

    public static readonly DEFAULT_PER_PAGE: number = 20;
    public static readonly DEFAULT_QUERY: string = `?${ConstantFilters.PER_PAGE}=${FeedService.DEFAULT_PER_PAGE}`;

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

        return rest.get(`/feeds?${query.toString()}`).pipe(
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
        return rest.post('/feeds', feed).pipe(
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

export default new FeedService();