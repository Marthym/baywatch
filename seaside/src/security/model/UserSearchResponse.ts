import {User} from "@/security/model/User";

export type UserSearchResponse = {
    userSearch?: {
        totalCount: number
        entities: User[]
    }
}