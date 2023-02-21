import {Team} from "@/teams/model/Team.type";

export type TeamsSearchResponse = {
    teamsSearch?: {
        totalCount: number
        entities: Team[]
    }
}