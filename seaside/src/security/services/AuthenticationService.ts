import { Observable } from 'rxjs';
import { User } from '@/security/model/User';
import { map, take } from 'rxjs/operators';
import { Session } from '@/security/model/Session';
import { send } from '@/common/services/GraphQLClient';

export class AuthenticationService {

    private static readonly LOGIN_REQUEST = `#graphql
    mutation Login($username: String!, $password: String!) {
        login(username: $username, password: $password) {
            _id login name roles mail
        }
    }`;
    private static readonly LOGOUT_REQUEST = 'mutation {logout}';

    login(username: string, password: string): Observable<User> {
        return send<{ login: User }>(AuthenticationService.LOGIN_REQUEST, { username: username, password: password },
        ).pipe(
            map(response => response.data.login),
            take(1),
        );
    }

    logout(): Observable<void> {
        return send(AuthenticationService.LOGOUT_REQUEST).pipe(
            map(() => undefined),
            take(1),
        );
    }
}

const SESSION_REFRESH_REQUEST = `#graphql
mutation {
    refreshSession {
        user { _id login mail name roles }
        settings { preferredLocale autoread newsViewMode }
        maxAge
    }
}`;

export function refresh(): Observable<Session> {
    return send<{ refreshSession: Session }>(SESSION_REFRESH_REQUEST).pipe(
        map(response => {
            if (response.errors && response.errors.length !== 0) {
                throw response.errors[0];
            } else {
                return response.data.refreshSession;
            }
        }),
        take(1),
    );
}

export default new AuthenticationService();