import { EMPTY, Observable, of } from 'rxjs';
import { map, switchMap, take } from 'rxjs/operators';
import { HttpStatusError } from '@/common/errors/HttpStatusError';
import { News } from '@/techwatch/model/News.type';
import { Mark } from '@/techwatch/model/Mark.enum';
import rest from '@/common/services/RestWrapper';
import { Infinite } from '@/services/model/Infinite';
import { NewsState } from '@/techwatch/model/NewsState.type';
import { NewsSearchRequest } from '@/techwatch/model/NewsSearchRequest.type';
import { SandSideError } from '@/common/errors/SandSideError';
import { GraphqlResponse } from '@/common/model/GraphqlResponse.type';
import { send } from '@/common/services/GraphQLClient';

export function newsMark(id: string, mark: Mark): Observable<NewsState> {
    return rest.put(`/news/${id}/mark/${mark}`).pipe(
        switchMap(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new HttpStatusError(response.status, `Error while mark news ${id} as ${mark}.`);
            }
        }),
        take(1),
    );
}

export class NewsService {

    public static readonly DEFAULT_PER_PAGE: number = 20;

    private static readonly NEWS_SEARCH_ANONYMOUS_REQUEST = `#graphql
    query LoadNewsListAnonPage {
        newsSearch(_pp: ${NewsService.DEFAULT_PER_PAGE}, _s: "-publication") {
            totalCount
            entities {
                id title description publication image imgd imgm link
                feeds { id name }
            }
        }
    }`;

    private static readonly NEWS_SEARCH_REQUEST = `#graphql
    query LoadNewsListPage (
        $_p: Int, $_pp: Int = ${NewsService.DEFAULT_PER_PAGE}, $_from: Int, $_to: Int, $_s: String = "-publication",
        $id: ID, $title: String, $description: String, $publication: String, $feeds: [String],
        $tags: [String] $read: Boolean, $shared: Boolean, $popular: Boolean, $keep: Boolean) {
        newsSearch(_p: $_p, _pp: $_pp, _from: $_from, _to: $_to, _s: $_s,
            id: $id, title: $title, description: $description, publication: $publication, feeds: $feeds,
            tags: $tags read: $read, shared: $shared, popular: $popular, keep: $keep) {
            totalCount
            entities {
                id title description publication image imgd imgm link
                feeds { id name }
                state { read shared keep }
                popularity { score }
            }
        }
    }`;

    getAnonymousNews(): Observable<Infinite<News>> {
        return send(NewsService.NEWS_SEARCH_ANONYMOUS_REQUEST).pipe(
            map(response => NewsService.graphResponseToInfinite(response)),
            take(1),
        );
    }

    /**
     * Get the {@link News} from backend
     *
     * @param page The to display
     * @param query The possible query parameters
     */
    getNews(query: NewsSearchRequest, page = 0): Observable<Infinite<News>> {
        if (page > 0) {
            query._p = page;
        }
        return send(NewsService.NEWS_SEARCH_REQUEST, query).pipe(
            map(response => NewsService.graphResponseToInfinite(response)),
            take(1),
        );
    }

    private static graphResponseToInfinite<G, T>(response: GraphqlResponse<G>): Infinite<T> {
        if (!response.errors || response.errors.length === 0) {
            const totalCount: number = response.data.newsSearch.totalCount || -1;
            const data: Observable<T[]> = of(response.data.newsSearch.entities as T[]);
            return {
                total: totalCount,
                data: data,
            };
        } else {
            throw new SandSideError(response.errors[0].extension[0].classification, 'Error while getting news.');
        }
    }

    unmark(id: string, mark: Mark): Observable<NewsState> {
        return rest.put(`/news/${id}/unmark/${mark}`).pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    return EMPTY;
                }
            }),
            take(1),
        );
    }
}

export default new NewsService();