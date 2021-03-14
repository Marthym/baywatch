import {Observable, throwError} from "rxjs";
import {fromFetch} from "rxjs/fetch";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {User} from "@/services/model/User";
import {catchError, map, switchMap, take} from "rxjs/operators";

export default class UserService {

    serviceBaseUrl: string;

    constructor(baseURL: string) {
        this.serviceBaseUrl = baseURL;
    }

    login(username: string, password: string): Observable<User> {
        const data = new FormData();
        data.append("username", username);
        data.append("password", password);
        return fromFetch(`${this.serviceBaseUrl}/auth/login`, {
            method: "POST",
            body: data
        }).pipe(
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
        return fromFetch(`${this.serviceBaseUrl}/auth/logout`).pipe(
            map(response => {
                if (!response.ok) {
                    throw new HttpStatusError(response.status, `Error while login out user !`);
                }
                return null;
            }),
            map(() => localStorage.removeItem('user')),
            take(1)
        );
    }

    refresh(): Observable<User> {
        return fromFetch(`${this.serviceBaseUrl}/auth/refresh`).pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new HttpStatusError(response.status, `Error while refreshing token.`);
                }
            }),
            map(user => this.save(user)),
            catchError(err => {
                localStorage.removeItem('user');
                return throwError(err);
            }),
            take(1)
        );
    }

    save(user: User): User {
        const parsed = JSON.stringify(user);
        localStorage.setItem('user', parsed);
        return user;
    }

    get(): User | undefined {
        const user = localStorage.getItem('user');
        if (user) {
            try {
                return JSON.parse(user);
            } catch (e) {
                localStorage.removeItem('user');
                throw e;
            }
        }
    }
}