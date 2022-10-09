import {Observable, switchMap} from "rxjs";
import {SearchEntry, SearchIndexResponse, SearchResultType} from "@/layout/model/SearchResult.type";
import {map, take} from "rxjs/operators";
import {Feed} from "@/layout/model/Feed.type";
import {send} from "@/common/services/GraphQLClient";

export class SearchService {
    private static readonly FEEDS_SEARCH_INDEX_REQUEST = `#graphql
    query LoadNewsListAnonPage($q: String) {
        searchIndex(q: $q) {
            type id
        }
    }`
    private static readonly FEEDS_DATA_REQUEST = `#graphql
    query SearchFeedsQuery ($ids: [ID]) {
        feedsSearch(id: $ids) {
            entities { id name url }
        }
    }`

    public search(q: string): Observable<SearchEntry[]> {
        return send<SearchIndexResponse>(SearchService.FEEDS_SEARCH_INDEX_REQUEST, {q: q}).pipe(
            switchMap(response => SearchService.getFeeds(response.data.searchIndex.map(si => si.id))),
            map(feeds => {
                return feeds.map(f => ({
                    id: f.id,
                    type: SearchResultType.FEED,
                    name: f.name,
                    url: f.url
                } as SearchEntry))
            }),
            take(1)
        );
    }

    private static getFeeds(ids: string[]): Observable<Feed[]> {
        return send(SearchService.FEEDS_DATA_REQUEST, {ids: ids}).pipe(
            take(1),
            map(response => ids
                .filter(i => response.data.feedsSearch.entities.findIndex(f => f.id === i) >= 0)
                .map(i => response.data.feedsSearch.entities.find(f => f.id === i))),
        );
    }
}

export default new SearchService();