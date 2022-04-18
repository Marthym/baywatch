import {Observable, switchMap} from "rxjs";
import {Popularity} from "@/techwatch/model/Popularity.type";
import rest from "@/services/http/RestWrapper";
import {take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";

export class PopularNewsService {

    public get(ids: string[]): Observable<Popularity> {
        const query = new URLSearchParams();
        ids.forEach(id => query.append('ids', id));
        return rest.get(`/news/popularity?${query.toString()}`).pipe(
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

export default new PopularNewsService();