import { map, take } from 'rxjs/operators';
import { Feed } from '@/configuration/model/Feed.type';
import { Page } from '@/common/model/Page';
import { Observable, of, throwError } from 'rxjs';
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
            entities {_id name description location tags error {
                level since message
            }}
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
        const resolvedPage: number = (options._p && options._p > 0) ? options._p : 0;
        return send<SearchFeedsResponse>(FeedService.FEEDS_SEARCH_REQUEST, options).pipe(
            map(res => {
                return {
                    currentPage: resolvedPage,
                    totalPage: Math.ceil(
                        res.data.feedsSearch.totalCount / (options._pp ?? FeedService.DEFAULT_PER_PAGE)),
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

const FEED_UPDATE = `#graphql
mutation FeedUpdate($id: ID, $name: String, $description: String, $tags: [String]) {
    feedUpdate(id: $id, name: $name, description: $description, tags: $tags) {_id name}
}`;

export function feedUpdate(id: string, feed: Pick<Feed, 'name' | 'description' | 'tags'>): Observable<Feed> {
    const { name, description, tags } = feed;
    if (id === undefined) {
        return throwError(() => new Error('Feed id is mandatory !'));
    }

    return send<{ feedUpdate: Feed }>(FEED_UPDATE, { id, name, description, tags }).pipe(
        map(data => data.data.feedUpdate),
        take(1),
    );
}

const FEED_ADD_AND_SUBSCRIBE = `#graphql
mutation FeedAddAndSubscribe($feed: FeedForm) {
    feedAddAndSubscribe(feed: $feed) {_id name}
}`;

export function feedAddAndSubscribe(feed: Pick<Feed, 'name' | 'description' | 'tags' | 'location'>): Observable<Feed> {
    const { name, description, tags, location } = feed;

    return send<{ feedAddAndSubscribe: Feed }>(FEED_ADD_AND_SUBSCRIBE, {
        feed: {
            name,
            description,
            tags,
            location,
        },
    }).pipe(
        map(data => data.data.feedAddAndSubscribe),
        take(1),
    );
}

const FEED_DELETE = `#graphql
mutation FeedDelete($ids: [ID]) {
    feedDelete(ids: $ids) {_id name}
}`;

export function feedDelete(ids: string[]): Observable<Pick<Feed, '_id' | 'name'>[]> {
    return send<{ feedDelete: Feed[] }>(FEED_DELETE, { ids }).pipe(
        map(data => data.data.feedDelete),
        take(1),
    );
}

const SCRAP_FEED_HEAD_REQUEST = `#graphql
query ScrapFeedHeader($link: URI!) {
    scrapFeedHeader(link: $link) {
        title
        description
    }
}`;

export function feedFetchInformation(link?: string): Observable<Feed> {
    if (link === undefined) {
        return throwError(() => new Error('Link is mandatory !'));
    } else if (!URL_PATTERN.test(link)) {
        return throwError(() => new Error('Argument link must be a valid URL !'));
    }

    return send<ScrapFeedHeaderResponse>(SCRAP_FEED_HEAD_REQUEST, { link: link }).pipe(
        map(data => data.data.scrapFeedHeader),
        map((atom: AtomFeed) => ({
            name: atom.title,
            description: atom.description,
        } as Feed)),
        take(1),
    );
}

export default new FeedService();