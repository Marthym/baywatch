import {Observable} from 'rxjs';
import {switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {Statistics} from "@/services/model/Statistics";
import rest from '@/services/http/RestWrapper';

export class StatsService {

    getBaywatchStats(): Observable<Statistics> {
        return rest.get('/stats').pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new HttpStatusError(response.status, `Error while getting news.`);
                }
            }),
            take(1)
        );
    }
}

export default new StatsService();