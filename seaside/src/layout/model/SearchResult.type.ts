export enum SearchResultType {
    FEED = 'FEED', NEWS = 'NEWS'
}

export type SearchEntry = {
    type: SearchResultType,
    id: string,
    name: string,
    url: string,
}

export type SearchIndexResponse = {
    searchIndex: SearchEntry[],
}