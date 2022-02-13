import {from, Observable, throwError} from "rxjs";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {map, switchMap, take} from "rxjs/operators";
import rest from '@/services/http/RestWrapper';
import {Page} from "@/services/model/Page";
import {ConstantFilters, ConstantHttpHeaders} from "@/constants";
import {User} from "@/services/model/User";

export class UserService {
    public static readonly DEFAULT_PER_PAGE: number = 20;
    public static readonly DEFAULT_QUERY: string = `?${ConstantFilters.PER_PAGE}=${UserService.DEFAULT_PER_PAGE}&_s=name`;

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
}

export default new UserService();