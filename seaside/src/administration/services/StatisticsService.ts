import {switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/common/errors/HttpStatusError";
import rest from '@/common/services/RestWrapper';
import {Observable} from "rxjs";
import {Counter} from "@/administration/model/Counter.type";

export class StatisticsService {
    get(): Observable<Counter[]> {
        return rest.get('/stats').pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json() as Promise<Counter[]>;
                } else {
                    throw new HttpStatusError(response.status, `Error while getting news.`);
                }
            }),
            take(1)
        );
    }
}

export default new StatisticsService();