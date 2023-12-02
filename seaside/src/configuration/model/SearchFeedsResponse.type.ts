import {Feed} from "@/configuration/model/Feed.type";
import {SearchRequest} from "@/common/model/SearchRequest.type";

export type SearchFeedsResponse = {
    feedsSearch: {
        totalCount: number,
        entities: Feed[]
    }
}

export type SearchFeedsRequest = SearchRequest & {
    id?: string,
    name?: string,
    description?: string,
    tags?: string[]
}