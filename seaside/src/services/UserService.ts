import {Observable} from "rxjs";
import {fromFetch} from "rxjs/fetch";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {User} from "@/services/model/User";
import {map, switchMap, take} from "rxjs/operators";

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