import {Observable, throwError} from "rxjs";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {User} from "@/services/model/User";
import {catchError, map, switchMap, take} from "rxjs/operators";
import {Session} from "@/services/model/Session";
import rest from '@/services/http/RestWrapper';

export class UserService {

    login(username: string, password: string): Observable<User> {
        const data = new FormData();
        data.append("username", username);
        data.append("password", password);
        return rest.post('/auth/login', data).pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new HttpStatusError(response.status, `Error while getting news.`);
                }
            }),
            take(1)
        );
    }

    logout(): Observable<void> {
        return rest.delete('/auth/logout').pipe(
            map(response => {
                if (!response.ok) {
                    throw new HttpStatusError(response.status, `Error while login out user !`);
                }
            }),
            take(1)
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
                return throwError(err);
            }),
            take(1)
        );
    }
}

export default new UserService();