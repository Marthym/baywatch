import {Observable} from 'rxjs';
import {fromFetch} from "rxjs/fetch";
import {switchMap} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";

export default class NewsService {
    serviceBaseUrl: string;

    constructor(baseURL: string) {
        this.serviceBaseUrl = baseURL;
    }

    getNews(): Observable<Array<News>> {
        return fromFetch(`${this.serviceBaseUrl}/news`).pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new HttpStatusError(response.status, `Error while getting news.`);
                }
            })
        );
    }
}