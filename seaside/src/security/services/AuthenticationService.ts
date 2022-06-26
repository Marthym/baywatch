import {Observable, throwError} from "rxjs";
import {HttpStatusError} from "@/common/errors/HttpStatusError";
import {User} from "@/security/model/User";
import {catchError, map, switchMap, take} from "rxjs/operators";
import {Session} from "@/security/model/Session";
import rest from '@/common/services/RestWrapper';
import gql from '@/common/services/GraphqlWrapper';

export class AuthenticationService {

    private static readonly LOGIN_REQUEST = `mutation Login($username: String!, $password: String!) {
        login(username: $username, password: $password) {
            _id login name role mail
        }
    }`;
    private static readonly REFRESH_REQUEST = `mutation Login($username: String!, $password: String!) {
        login(username: $username, password: $password) {
            _id login name role mail
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
        return rest.put('/auth/refresh').pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new HttpStatusError(response.status, `Error while refreshing token.`);
                }
            }),
            catchError(err => {
                return throwError(() => err);
            }),
            take(1)
        );
    }
}

export default new AuthenticationService();