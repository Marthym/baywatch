import {Observable, of} from 'rxjs';
import {fromFetch} from "rxjs/fetch";
import {catchError, switchMap} from "rxjs/operators";

export default class NewsService {
    serviceBaseUrl: string;

    constructor(baseURL: string) {
        this.serviceBaseUrl = baseURL;
    }

    getNews(): Observable<Array<News> | any> {
        return fromFetch(`${this.serviceBaseUrl}/news`).pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    return of({error: true, message: `Error ${response.status}`});
                }
            }),
            catchError(err => {
                // Network or other error, handle appropriately
                console.error(err);
                return of({error: true, message: err.message})
            })
        );
    }
}