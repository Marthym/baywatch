import {switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {from, Observable} from "rxjs";
import rest from '@/services/http/RestWrapper';

export class TagsService {
    /**
     * List all available tags from backend
     */
    list(): Observable<string[]> {
        return rest.get('/tags').pipe(
            switchMap(response => {
                if (response.ok) {
                    const data: Observable<string[]> = from(response.json());
                    return data;
                } else {
                    throw new HttpStatusError(response.status, `Error while getting tags.`);
                }
            }),
            take(1)
        );
    }
}

export default new TagsService();