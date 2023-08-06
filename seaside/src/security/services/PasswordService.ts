import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { send } from '@/common/services/GraphQLClient';
import { PasswordEvaluation } from '@/security/model/PasswordEvaluation.type';

type PasswordCheckResponse = { checkPasswordStrength: PasswordEvaluation };

const PASSWORD_CHECK_REQUEST = `#graphql
query CheckPasswordStrength($password: String){
    checkPasswordStrength(password: $password) {
        entropy isSecure message
    }
}`;

export function passwordCheckStrength(password: string): Observable<PasswordEvaluation> {
    return send<PasswordCheckResponse>(PASSWORD_CHECK_REQUEST, { password: password }).pipe(
        map(data => data.data.checkPasswordStrength),
        take(1),
    );
}
