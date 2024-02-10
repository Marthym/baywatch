import {Observable} from "rxjs";

export type Page<T> = {
    currentPage: number;
    totalPage: number;
    data: Observable<T[]>
}