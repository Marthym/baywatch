import { Observable } from 'rxjs';
import { User } from '@/security/model/User';
import { map, take } from 'rxjs/operators';
import { Session } from '@/security/model/Session';
import { send } from '@/common/services/GraphQLClient';

const LOGIN_REQUEST = `#graphql
mutation Login($username: String!, $password: String!) {
    login(username: $username, password: $password) {
        _id login name roles mail
    }
}`;
const LOGOUT_REQUEST = 'mutation {logout}';

export function authenticationLogin(username: string, password: string): Observable<User> {
    return send<{ login: User }>(LOGIN_REQUEST, { username: username, password: password },
    ).pipe(
        map(response => response.data.login),
        take(1),
    );
}

export function authenticationLogout(): Observable<void> {
    return send(LOGOUT_REQUEST).pipe(
        map(() => undefined),
        take(1),
    );
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

const ASK_FOR_PASSWORD_RESET_REQUEST = `#graphql
query AskForPasswordReset($identifier: String!) {
    askForPasswordReset(identifier: $identifier)
}`;

export function askForPasswordReset(identifier: string): Observable<void> {
    return send<{ askForPasswordReset: void }>(ASK_FOR_PASSWORD_RESET_REQUEST, { identifier }).pipe(
        map(response => {
            if (response.errors && response.errors.length !== 0) {
                throw response.errors[0];
            } else {
                return;
            }
        }),
        take(1),
    );
}