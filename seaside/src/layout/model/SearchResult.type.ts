export enum SearchResultType {
    FEED = 'FEED', NEWS = 'NEWS'
}

export type SearchEntry = {
    id: string,
    _createdBy?: string,
    type: SearchResultType,
    name: string,
    url: string,
}

export type SearchIndexResponse = {
    searchIndex: SearchEntry[],
}