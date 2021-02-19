import {Observable} from 'rxjs';
import {fromFetch} from "rxjs/fetch";
import {switchMap} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {Statistics} from "@/services/model/Statistics";

export default class StatsService {

    serviceBaseUrl: string;

    constructor(baseURL: string) {
        this.serviceBaseUrl = baseURL;
    }

    getBaywatchStats(): Observable<Statistics> {
        return fromFetch(`${this.serviceBaseUrl}/stats`).pipe(
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