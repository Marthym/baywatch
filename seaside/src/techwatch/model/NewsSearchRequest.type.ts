import {SearchRequest} from "@/common/model/SearchRequest.type";

export interface NewsSearchRequest extends SearchRequest {
    id?: string
    title?: string
    description?: string
    publication?: string
    feeds?: string[]
    tags?: string[]
    read?: boolean
    shared?: boolean
    popular?: boolean
}
