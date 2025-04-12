import { Observable, of } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { send } from '@/common/services/GraphQLClient';
import { Page } from '@/common/model/Page';
import { SearchRequest } from '@/common/model/SearchRequest.type';
import { RawFeed } from '@/administration/model/RawFeed.type';

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
            _id name url icon description lastETag lastWatch
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
