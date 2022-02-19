import {Observable} from "rxjs";

export type Infinite<T> = {
    total: number;
    data: Observable<T[]>
}