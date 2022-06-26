import {News} from "@/techwatch/model/News.type";

export type NewsSearchResponse = {
    newsSearch?: {
        totalCount: number
        entities: News[]
    }
}
