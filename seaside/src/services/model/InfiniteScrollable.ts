import {Observable} from "rxjs";

export default interface InfiniteScrollable {
    loadNextPage(): Observable<Element>;
}