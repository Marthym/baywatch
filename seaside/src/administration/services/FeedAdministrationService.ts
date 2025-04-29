import { Observable, of, switchMap, throwError } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { send } from '@/common/services/GraphQLClient';
import { Page } from '@/common/model/Page';
import { SearchRequest } from '@/common/model/SearchRequest.type';
import { RawFeed } from '@/administration/model/RawFeed.type';
import { URL_PATTERN } from '@/common/services/RegexPattern';

const DEFAULT_PER_PAGE = 20;
export type FindRawFeedsRequest = SearchRequest & {
    id?: string,
    name?: string,
    url?: string,
}
type FindRawFeedsResponse = {
    adminRawFeedFind: {
        totalCount: number,
        entities: RawFeed[]
    }
}

const ADMIN_RAW_FEED_FIND = `#graphql
query AdminRawFeedFind($_p: Int, $_pp: Int, $_from: Int, $_to: Int, $_s: String
    $_id: [ID], $name: String, $url: String) {
    adminRawFeedFind(
        _p: $_p, _pp: $_pp, _from: $_from, _to: $_to, _s: $_s
        _id: $_id, name: $name, url: $url) {
        totalCount entities {
            _id name url icon description lastETag lastWatch error {
                level since message
            }
        }
    }
}`;

export function adminFeedFind(options: FindRawFeedsRequest): Observable<Page<RawFeed>> {
    const resolvedPage: number = (options._p && options._p > 0) ? options._p : 0;
    const variables: FindRawFeedsRequest = {
        _p: options._p ?? 0,
        _pp: options._pp ?? DEFAULT_PER_PAGE,
    };
    return send<FindRawFeedsResponse>(ADMIN_RAW_FEED_FIND, variables).pipe(
        map(res => {
            return {
                currentPage: resolvedPage,
                totalPage: Math.ceil(
                    res.data.adminRawFeedFind.totalCount / (options._pp ?? DEFAULT_PER_PAGE)),
                data: of(res.data.adminRawFeedFind.entities),
            };
        }),
        take(1),
    );
}

const ADMIN_RAW_FEED_GET = `#graphql
query AdminRawFeedGet($_id: ID) {
    adminRawFeedGet(_id: $_id) {
        _id name url icon description lastETag lastWatch error {
            level since message
        }
    }
}`;

export function adminRawFeedGet(id: string): Observable<RawFeed> {
    return send<{ adminRawFeedGet: RawFeed }>(ADMIN_RAW_FEED_GET, { _id: id }).pipe(
        map(res => res.data.adminRawFeedGet),
        take(1),
    );
}

const ADMIN_RAW_FEED_DELETE = `#graphql
mutation AdminRawFeedDelete($_id: [ID]) {
    adminRawFeedDelete(_id: $_id)
}`;

export function adminRawFeedDelete(ids: string[]): Observable<void> {
    const variables = {
        _id: ids,
    };
    return send<{ adminRawFeedDelete: {} }>(ADMIN_RAW_FEED_DELETE, variables).pipe(
        switchMap(res => of(undefined)),
        take(1),
    );
}

const ADMIN_RAW_FEED_UPDATE = `#graphql
mutation AdminRawFeedUpdate($_id: ID!, $rawFeed: RawFeedForm) {
    adminRawFeedUpdate(_id: $_id, rawFeed: $rawFeed) {
        _id name url icon description lastETag lastWatch error {
            level since message
        }
    }
}`;

export function adminFeedUpdate(id: string, feed: RawFeed): Observable<RawFeed> {
    const variables = {
        _id: id,
        rawFeed: feed,
    };
    return send<{ adminRawFeedUpdate: RawFeed }>(ADMIN_RAW_FEED_UPDATE, variables).pipe(
        map(res => res.data.adminRawFeedUpdate),
        take(1),
    );
}

const ADMIN_RAW_FEED_CREATE = `#graphql
mutation AdminRawFeedUpdate($rawFeed: RawFeedForm) {
    adminRawFeedCreate(rawFeed: $rawFeed) {
        _id name url icon description lastETag lastWatch error {
            level since message
        }
    }
}`;

export function adminRawFeedCreate(feed: RawFeed): Observable<RawFeed> {
    const variables = { rawFeed: feed };
    return send<{ adminRawFeedCreate: RawFeed }>(ADMIN_RAW_FEED_CREATE, variables).pipe(
        map(res => res.data.adminRawFeedCreate),
        take(1),
    );
}

const SCRAP_FEED_HEAD_REQUEST = `#graphql
query ScrapFeedHeader($link: URI!) {
    scrapFeedHeader(link: $link) {
        title description icon link
    }
}`;

type AtomFeed = {
    title: string
    description: string
    icon: string
    link: string
}

export function adminRawFeedScrap(link: string): Observable<RawFeed> {
    if (link === undefined) {
        return throwError(() => new Error('Link is mandatory !'));
    } else if (!URL_PATTERN.test(link)) {
        return throwError(() => new Error('Argument link must be a valid URL !'));
    }

    return send<{ scrapFeedHeader: AtomFeed }>(SCRAP_FEED_HEAD_REQUEST, { link: link }).pipe(
        map(data => data.data.scrapFeedHeader),
        map((atom: AtomFeed) => ({
            name: atom.title,
            description: atom.description,
            icon: atom.icon,
            url: atom.link,
        } as RawFeed)),
        take(1),
    );
}