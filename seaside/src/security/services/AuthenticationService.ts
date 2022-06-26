import {Observable} from "rxjs";
import {User} from "@/security/model/User";
import {map, take} from "rxjs/operators";
import {Session} from "@/security/model/Session";
import gql from '@/common/services/GraphqlWrapper';
import {GraphqlResponse} from "@/common/model/GraphqlResponse.type";

export class AuthenticationService {

    private static readonly LOGIN_REQUEST = `#graphql
        mutation Login($username: String!, $password: String!) {
            login(username: $username, password: $password) {
                _id login name role mail
            }
        }`;
    private static readonly REFRESH_REQUEST = `#graphql
        mutation {
            refreshSession {
                user { _id login mail name role }
                maxAge
            }
        }`;
    private static readonly LOGOUT_REQUEST = 'mutation {logout}';

    login(username: string, password: string): Observable<User> {
        return gql.send(AuthenticationService.LOGIN_REQUEST, {username: username, password: password}
        ).pipe(
            map(response => response.data.login),
            take(1),
        );
    }

    logout(): Observable<void> {
        return gql.send(AuthenticationService.LOGOUT_REQUEST).pipe(
            map(() => undefined),
            take(1),
        );
    }

    refresh(): Observable<Session> {
        return gql.send(AuthenticationService.REFRESH_REQUEST).pipe(
            map((response: GraphqlResponse<{ refreshSession: Session }>) => {
                if (response.errors && response.errors.length !== 0) {
                    throw response.errors[0];
                } else {
                    return response.data.refreshSession;
                }
            }),
            take(1)
        );
    }
}

export default new AuthenticationService();