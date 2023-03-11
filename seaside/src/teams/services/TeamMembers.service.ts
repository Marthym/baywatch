import {Observable} from "rxjs";
import {send} from "@/common/services/GraphQLClient";
import {map, take} from "rxjs/operators";
import {Member} from "@/teams/model/Member.type";
import {User} from "@/teams/model/User.type";

/**
 * Search for Teams
 */
const TEAM_MEMBERS_LIST_REQUEST = `#graphql
query ListTeamMembers($teamId: ID){
    teamMembersList(teamId: $teamId) {
        _id _user {_id login roles} pending
    }
}`

export function teamMemberList(teamId: string): Observable<Member[]> {
    return send<{ teamMembersList: Member[] }>(TEAM_MEMBERS_LIST_REQUEST, {teamId: teamId}).pipe(
        map(data => data.data.teamMembersList),
        take(1),
    );
}

const USER_FILTER_REQUEST = `#graphql
query FilterAvailableUsers($_p: Int, $_pp: Int, $term: String){
    userSearch(_p: $_p, _pp: $_pp, login: $term) {
        entities {
            _id login
        }
    }
}`

export function teamMemberAvailable(term: string): Observable<User[]> {
    const vars = {
        term: `^${term}`,
        _pp: 10
    };
    return send<{ userSearch: { entities: User[] } }>(USER_FILTER_REQUEST, vars).pipe(
        map(data => data.data.userSearch.entities),
        take(1),
    );
}

const TEAM_MEMBERS_ADD_REQUEST = `#graphql
mutation AddTeamMembers($teamId: ID, $members: [ID]){
    teamMembersAdd(_id: $teamId, membersIds: $members) {
        _id _user {_id login roles} pending
    }
}`

export function teamMemberAdd(teamId: string, members: string[]): Observable<Member[]> {
    const vars = {teamId: teamId, members: members};
    return send<{ teamMembersAdd: Member[] }>(TEAM_MEMBERS_ADD_REQUEST, vars).pipe(
        map(data => data.data.teamMembersAdd),
        take(1),
    );
}

const TEAM_MEMBERS_REMOVE_REQUEST = `#graphql
mutation RemoveTeamMembers($teamId: ID, $members: [ID]){
    teamMembersDelete(_id: $teamId, membersIds: $members) {
        _id _user {_id login roles} pending
    }
}`

export function teamMemberDelete(teamId: string, members: string[]): Observable<Member[]> {
    const vars = {teamId: teamId, members: members};
    return send<{ teamMembersAdd: Member[] }>(TEAM_MEMBERS_REMOVE_REQUEST, vars).pipe(
        map(data => data.data.teamMembersAdd),
        take(1),
    );
}