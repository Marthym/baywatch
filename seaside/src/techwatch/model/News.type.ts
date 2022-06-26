import {NewsState} from "@/techwatch/model/NewsState.type";
import {Feed} from "@/techwatch/model/Feed.type";
import {Popularity} from "@/techwatch/model/Popularity.type";

export type News = {
    id: string
    title: string
    image: string
    description: string
    publication: string
    link: string
    feeds: Feed[]
    tags?: string
    state: NewsState
    popularity?: Popularity
}
