import {from, Observable, throwError} from "rxjs";
import {HttpStatusError} from "@/common/errors/HttpStatusError";
import {map, switchMap, take} from "rxjs/operators";
import rest from '@/common/services/RestWrapper';
import {Page} from "@/services/model/Page";
import {ConstantFilters, ConstantHttpHeaders} from "@/constants";
import {User} from "@/security/model/User";
import {OpPatch} from "json-patch";

export class UserService {
    public static readonly DEFAULT_PER_PAGE: number = 20;
    public static readonly DEFAULT_QUERY: string = `?${ConstantFilters.PER_PAGE}=${UserService.DEFAULT_PER_PAGE}&_s=login`;

    //TODO: Refactor to avoid duplication with same method in feed Service
    list(page = 0, query: URLSearchParams = new URLSearchParams(UserService.DEFAULT_QUERY)): Observable<Page<User>> {
        const resolvedPage = (page > 0) ? page : 0;
        query.set(ConstantFilters.PAGE, String(resolvedPage));
        let resolvedPerPage = query.get(ConstantFilters.PER_PAGE);
        if (resolvedPerPage === null) {
            resolvedPerPage = String(UserService.DEFAULT_PER_PAGE);
            query.append(ConstantFilters.PER_PAGE, resolvedPerPage);
        }

        return rest.get(`/users?${query.toString()}`).pipe(
            map(response => {
                if (response.ok) {
                    const totalCount = parseInt(response.headers.get(ConstantHttpHeaders.X_TOTAL_COUNT) || "-1");
                    const data: Observable<User[]> = from(response.json());
                    return {
                        currentPage: resolvedPage,
                        totalPage: Math.ceil(totalCount / Number(resolvedPerPage)),
                        data: data
                    };
                } else {
                    throw new HttpStatusError(response.status, `Error while getting news.`);
                }
            }),
            take(1)
        );
    }

    add(user: User): Observable<User> {
        return rest.post('/users', user).pipe(
            switchMap(response => {
                if (response.ok) {
                    return from(response.json());
                } else {
                    return from(response.json()).pipe(switchMap(j =>
                        throwError(() => new HttpStatusError(response.status, j.message))));
                }
            }),
            take(1)
        );
    }

    update(user: User): Observable<User> {
        return rest.put(`/users/${user._id}`, user).pipe(
            switchMap(response => {
                if (response.ok) {
                    return from(response.json());
                } else {
                    return from(response.json()).pipe(switchMap(j =>
                        throwError(() => new HttpStatusError(response.status, j.message))));
                }
            }),
            take(1)
        );
    }

    remove(id: string): Observable<User> {
        return rest.delete(`/users/${id}`).pipe(
            switchMap(response => {
                if (response.ok) {
                    return from(response.json());
                } else {
                    throw new HttpStatusError(response.status, `Error while unsubscribing user ${id}`);
                }
            }),
            take(1)
        );
    }

    bulkRemove(ids: string[]): Observable<User> {
        const jsonPatch: OpPatch[] = [];
        ids.forEach(id => jsonPatch.push({op: 'remove', path: `/users/${id}`}))
        return rest.patch(`/users`, jsonPatch).pipe(
            switchMap(response => {
                if (response.ok) {
                    return from(response.json());
                } else {
                    throw new HttpStatusError(response.status, `Error while unsubscribing feeds ${ids}`);
                }
            }),
            take(1)
        );
    }
}

export default new UserService();