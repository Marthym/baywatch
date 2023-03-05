import {Observable} from "rxjs";
import {send} from "@/common/services/GraphQLClient";
import {map, take} from "rxjs/operators";
import {Member} from "@/teams/model/Member.type";

/**
 * Search for Teams
 */
const TEAM_MEMBERS_LIST_REQUEST = `#graphql
query ListTeamMembers($teamId: ID){
    teamMembersList(teamId: $teamId) {
        _id _user {_id name roles} pending
    }
}`

export function teamMemberList(teamId: string): Observable<Member[]> {
    return send<{ teamMembersList: Member[] }>(TEAM_MEMBERS_LIST_REQUEST, {teamId: teamId}).pipe(
        map(data => data.data.teamMembersList),
        take(1),
    );
}
