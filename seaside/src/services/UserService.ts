import {Observable, throwError} from "rxjs";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {User} from "@/services/model/User";
import {catchError, map, shareReplay, switchMap, take, tap} from "rxjs/operators";
import {Session} from "@/services/model/Session";
import rest from '@/services/http/RestWrapper';

export interface UserListener {
    onUserChange(data: User): void;
}

export class UserService {

    private userListeners: UserListener[] = [];
    private readonly cache$: Observable<Session>;
    private reloadFunction: VoidFunction = () => {console.warn('no reload function!')};

    constructor() {
        this.cache$ = this.refresh().pipe(
            shareReplay(1),
        );
    }

    /**
     * Register the function call on reload
     * This allow others components to reload news list
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

    registerUserListener(consumer: UserListener): void {
        this.userListeners.push(consumer);
    }

    unregisterUserListener(consumer: UserListener): void {
        const idx = this.userListeners.indexOf(consumer);
        this.userListeners.splice(idx);
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
            map(user => this.save(user)),
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
            tap(session => this.save(session.user)),
            catchError(err => {
                return throwError(err);
            }),
            take(1)
        );
    }

    private save(user: User): User {
        this.userListeners.forEach(consumer => consumer.onUserChange(user))
        return user;
    }

    get(): Observable<User> {
        return this.cache$.pipe(
            map(s => s.user),
        );
    }
}

export default new UserService();