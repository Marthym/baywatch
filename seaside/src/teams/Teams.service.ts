import {ConstantFilters} from "@/constants";
import {Observable} from "rxjs";
import {Page} from "@/services/model/Page";
import {send} from "@/common/services/GraphQLClient";
import {map, take} from "rxjs/operators";
import {Team} from "@/teams/model/Team.type";
import {TeamsSearchResponse} from "@/teams/model/TeamsSearchResponse";

const DEFAULT_PER_PAGE: number = 20;
const DEFAULT_QUERY: string = `?${ConstantFilters.PER_PAGE}=${DEFAULT_PER_PAGE}&_s=name`;

const TEAMS_SEARCH_REQUEST = `#graphql
query SearchForTeams{
    teamsSearch {
        totalCount
        entities {_id
            _createdBy {_id name}
            _managers {_id name}
            name topic}
    }
}`

export function teamsList(page = 0, query: URLSearchParams = new URLSearchParams(DEFAULT_QUERY)): Observable<Page<Team>> {
    const resolvedPage = (page > 0) ? page : 0;
    let resolvedPerPage = query.get(ConstantFilters.PER_PAGE);
    if (resolvedPerPage) {
        resolvedPerPage = String(DEFAULT_PER_PAGE);
    }
    return send<TeamsSearchResponse>(TEAMS_SEARCH_REQUEST).pipe(
        map(data => data.data.teamsSearch),
        map(response => ({
                currentPage: resolvedPage,
                totalPage: Math.ceil(response.totalCount / Number(resolvedPerPage)),
                data: response.entities
            })
        ),
        take(1),
    );
}

const TEAMS_CREATE_REQUEST = `#graphql
mutation CreateNewTeam($name: String, $topic: String){
    teamCreate(name: $name, topic: $topic) {
        _id
        _createdBy {_id name}
        _managers {_id name}
        name topic
    }}`

export function teamCreate(name: string, topic: string): Observable<Team> {
    return send<{ teamCreate: Team }>(TEAMS_CREATE_REQUEST, {name: name, topic: topic}).pipe(
        map(data => data.data.teamCreate),
        take(1),
    );
}

const TEAMS_DELETE_REQUEST = `#graphql
mutation DeleteTeams($id: [ID]){
    teamDelete(id: $id) {
        _id name
    }}`

export function teamDelete(ids: string[]): Observable<Team[]> {
    return send<{ teamDelete: Team[] }>(TEAMS_DELETE_REQUEST, {id: ids}).pipe(
        map(data => data.data.teamDelete),
        take(1),
    );
}