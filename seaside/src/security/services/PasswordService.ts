import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { send } from '@/common/services/GraphQLClient';
import { PasswordEvaluation } from '@/security/model/PasswordEvaluation.type';
import { User } from '@/security/model/User';

type PasswordCheckResponse = { passwordCheckStrength: PasswordEvaluation };
type PasswordAnonymousCheckResponse = { passwordCheckAnonymous: PasswordEvaluation };
type PasswordGenerateResponse = { passwordGenerate: string[] };

const PASSWORD_CHECK_REQUEST = `#graphql
query CheckPasswordStrength($password: String){
    passwordCheckStrength(password: $password) {
        entropy isSecure message
    }
}`;

const PASSWORD_ANONYMOUS_CHECK_REQUEST = `#graphql
query PasswordCheckAnonymous($user: UserForm){
    passwordCheckAnonymous(user: $user) {
        entropy isSecure message
    }
}`;

const PASSWORD_GENERATE_REQUEST = `#graphql
query GeneratePasswords($count: Int) {
    passwordGenerate(number: $count)
}`;


export function passwordAnonymousCheckStrength(user: User): Observable<PasswordEvaluation> {
    const { _id, _createdAt, _loginAt, _loginIP, ...passwordCheckedProps } = user;
    return send<PasswordAnonymousCheckResponse>(PASSWORD_ANONYMOUS_CHECK_REQUEST, { user: passwordCheckedProps }).pipe(
        map(data => data.data.passwordCheckAnonymous),
        take(1),
    );
}

export function passwordCheckStrength(password: string): Observable<PasswordEvaluation> {
    return send<PasswordCheckResponse>(PASSWORD_CHECK_REQUEST, { password: password }).pipe(
        map(data => data.data.passwordCheckStrength),
        take(1),
    );
}

export function passwordGenerate(count: number): Observable<string[]> {
    return send<PasswordGenerateResponse>(PASSWORD_GENERATE_REQUEST, { count: count }).pipe(
        map(data => data.data.passwordGenerate),
        take(1),
    );
}

const PASSWORD_ASK_FOR_RESET_QUERY = `#graphql
query PasswordResetAskFor($identifier: String!) {
    passwordAskForReset(identifier: $identifier)
}`;

export function passwordAskForReset(identifier: string): Observable<void> {
    return send<{ passwordAskForReset: void }>(PASSWORD_ASK_FOR_RESET_QUERY, { identifier }).pipe(
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

const PASSWORD_RESET_MUTATION = `#graphql
mutation PasswordReset($token: String, $newPassword: String) {
    passwordReset(token: $token, newPassword: $newPassword)
}`;

export function passwordReset(token: string, newPassword: string): Observable<void> {
    return send<{ passwordReset: void }>(PASSWORD_RESET_MUTATION, { token, newPassword }).pipe(
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