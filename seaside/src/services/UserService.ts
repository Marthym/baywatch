import {Observable, throwError} from "rxjs";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {User} from "@/services/model/User";
import {catchError, map, shareReplay, switchMap, take} from "rxjs/operators";
import {Session} from "@/services/model/Session";
import rest from '@/services/http/RestWrapper';

export class UserService {

    private readonly cache$: Observable<Session>;
    private reloadFunction: VoidFunction = () => {
        console.warn('no reload function!')
    };

    constructor() {
        this.cache$ = this.refresh().pipe(
            shareReplay(1),
        );
    }

    /**
     * Register the function call on reload
     * This allows others components to reload news list
     *
     * @param apply [VoidFunction] The call function
     */
    registerReloadFunction(apply: VoidFunction): void {
        this.reloadFunction = apply;
    }

    reload(): void {
        if (this.reloadFunction) {
            this.reloadFunction();
        }
    }

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

    private refresh(): Observable<Session> {
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

    get(): Observable<User> {
        return this.cache$.pipe(
            map(s => s.user),
        );
    }
}

export default new UserService();