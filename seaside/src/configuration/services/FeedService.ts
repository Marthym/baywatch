import { map, switchMap, take } from 'rxjs/operators';
import { HttpStatusError } from '@/common/errors/HttpStatusError';
import { Feed } from '@/configuration/model/Feed.type';
import { Page } from '@/common/model/Page';
import { from, Observable, of, throwError } from 'rxjs';
import rest from '@/common/services/RestWrapper';
import { OpPatch } from 'json-patch';
import { AtomFeed, ScrapFeedHeaderResponse } from '@/configuration/model/GraphQLScraper.type';
import { SearchFeedsRequest, SearchFeedsResponse } from '@/configuration/model/SearchFeedsResponse.type';
import { send } from '@/common/services/GraphQLClient';
import { URL_PATTERN } from '@/common/services/RegexPattern';

export class FeedService {

    public static readonly DEFAULT_PER_PAGE: number = 20;

    private static readonly FEEDS_SEARCH_REQUEST = `#graphql
    query SearchFeedsQuery ($_p: Int = 0, $_pp: Int = ${FeedService.DEFAULT_PER_PAGE}, $_s: String = "name") {
        feedsSearch(_p: $_p, _pp: $_pp, _s: $_s) {
            totalCount
            entities {_id name location tags error {
                code since message
            }}
        }
    }`;

    private static readonly SCRAP_FEED_HEAD_REQUEST = `#graphql
    query ScrapFeedHeader($link: URI!) {
        scrapFeedHeader(link: $link) {
            title
            description
        }
    }`;

    private static readonly FEED_SUBSCRIBE = `#graphql
    mutation Subscription($feedId: ID) {
        subscribe(id: $feedId) {_id name}
    }`;

    /**
     * Search the {@link Feed} from backend depending on query parameters
     *
     * @param {SearchFeedsRequest} options SearchFeedsRequest The to display
     * @return The {@link Feed} page corresponding to the options
     */
    public list(options: SearchFeedsRequest): Observable<Page<Feed>> {
        const resolvedPage = (options._p > 0) ? options._p : 0;
        return send<SearchFeedsResponse>(FeedService.FEEDS_SEARCH_REQUEST, options).pipe(
            map(res => {
                return {
                    currentPage: resolvedPage,
                    totalPage: Math.ceil(
                        res.data.feedsSearch.totalCount / (options._pp | FeedService.DEFAULT_PER_PAGE)),
                    data: of(res.data.feedsSearch.entities).pipe(
                        map(feeds => {
                            feeds.forEach(feed => {
                                if (!feed.icon) {
                                    feed.icon = new URL(new URL(feed.location).origin + '/favicon.ico');
                                }
                                if (feed.error) {
                                    feed.error.since = new Date(feed.error.since);
                                }
                            });
                            return feeds;
                        }),
                    ),
                };
            }),
            take(1),
        );
    }

    public add(feed: Feed): Observable<Feed> {
        return rest.post('/feeds', feed).pipe(
            switchMap(this.responseToFeed),
            take(1),
        );
    }

    public update(feed: Feed, urlChange: boolean = true): Observable<string> {
        if (!urlChange) {
            return rest.put(`/feeds/${feed._id}`, feed).pipe(
                switchMap(this.responseToFeed),
                map((updatedFeed: Feed) => updatedFeed._id),
                take(1),
            );
        } else {
            const jsonPatch: OpPatch[] = [];
            jsonPatch.push({ op: 'remove', path: `/feeds/${feed._id}` });
            jsonPatch.push({ op: 'add', path: '/feeds', value: feed });

            return this.patch(jsonPatch).pipe(
                map(updated => updated.pop()),
            );
        }
    }

    public remove(id: string): Observable<Feed> {
        return rest.delete(`/feeds/${id}`).pipe(
            switchMap(this.responseToFeed),
            take(1),
        );
    }

    public bulkRemove(ids: string[]): Observable<number> {
        const jsonPatch: OpPatch[] = [];
        ids.forEach(id => jsonPatch.push({ op: 'remove', path: `/feeds/${id}` }));
        return this.patch(jsonPatch).pipe(
            map(deleted => deleted.length),
        );
    }

    private patch(payload: OpPatch[]): Observable<string[]> {
        return rest.patch('/feeds', payload).pipe(
            switchMap(this.responseToFeed),
            take(1),
        );
    }

    private responseToFeed(response: Response): Observable<any> {
        if (response.ok) {
            return from(response.json());
        } else {
            return from(response.json()).pipe(switchMap(j =>
                throwError(() => new HttpStatusError(response.status, j.message))));
        }
    }

    public fetchFeedInformation(link?: string): Observable<Feed> {
        if (link === undefined) {
            return throwError(() => new Error('Link is mandatory !'));
        } else if (!URL_PATTERN.test(link)) {
            return throwError(() => new Error('Argument link must be a valid URL !'));
        }

        return send<ScrapFeedHeaderResponse>(FeedService.SCRAP_FEED_HEAD_REQUEST, { link: link }).pipe(
            map(data => data.data.scrapFeedHeader),
            map((atom: AtomFeed) => ({
                name: atom.title,
                description: atom.description,
            } as Feed)),
            take(1),
        );
    }

    public subscribe(id: string): Observable<Feed> {
        if (id === undefined) {
            return throwError(() => new Error('Feed id is mandatory !'));
        }

        return send<{ subscribe: Feed }>(FeedService.FEED_SUBSCRIBE, { feedId: id }).pipe(
            map(data => data.data.subscribe),
            take(1),
        );
    }
}

export default new FeedService();