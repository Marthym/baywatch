import {ConstantFilters} from "@/constants";
import {Observable, of} from "rxjs";
import {Page} from "@/common/model/Page";
import {send} from "@/common/services/GraphQLClient";
import {map, take, tap} from "rxjs/operators";
import {Team} from "@/teams/model/Team.type";
import {TeamsSearchResponse} from "@/teams/model/TeamsSearchResponse";
import store from '@/store';
import {USER_ADD_ROLE_MUTATION} from "@/security/store/UserConstants";

const DEFAULT_PER_PAGE: number = 20;
const DEFAULT_QUERY: string = `?${ConstantFilters.PER_PAGE}=${DEFAULT_PER_PAGE}&_s=name`;

/**
 * Search for Teams
 */
const TEAMS_SEARCH_REQUEST = `#graphql
query SearchForTeams{
    teamsSearch {
        totalCount
        entities {_id
            _createdBy {_id name}
            _managers {_id name}
            _me {pending}
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
                data: of(response.entities),
            })
        ),
        take(1),
    );
}

/**
 * Create new Team
 */
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
        tap(team => store.commit(USER_ADD_ROLE_MUTATION, `MANAGER:${team._id}`))
    );
}

/**
 * Update a Team
 */
const TEAMS_UPDATE_REQUEST = `#graphql
mutation UpdateTeam($id: ID, $team: TeamForm){
    teamUpdate(id: $id, team: $team) {
        _id
        _createdBy {_id name}
        _managers {_id name}
        _me {pending}
        name topic
    }}`

export function teamUpdate(_id: string, team: Team): Observable<Team> {
    const teamForm = {
        name: team.name,
        topic: (team.topic.trim() !== "") ? team.topic : undefined,
    }
    return send<{ teamUpdate: Team }>(TEAMS_UPDATE_REQUEST, {id: _id, team: teamForm}).pipe(
        map(data => data.data.teamUpdate),
        take(1),
    );
}

/**
 * Delete teams
 */
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