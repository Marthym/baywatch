import {Observable} from "rxjs";

export type Page<T> = {
    totalCount: number;
    data: Observable<T[]>
}