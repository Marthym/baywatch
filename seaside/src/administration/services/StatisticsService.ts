import {switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {Statistics} from "@/administration/model/Statistics.type";
import rest from '@/services/http/RestWrapper';
import {Observable} from "rxjs";

export class StatisticsService {
    get(): Observable<Statistics> {
        return rest.get('/stats').pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json() as Promise<Statistics>;
                } else {
                    throw new HttpStatusError(response.status, `Error while getting news.`);
                }
            }),
            take(1)
        );
    }
}

export default new StatisticsService();