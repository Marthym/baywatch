import {Observable, of} from "rxjs";
import rest from "@/services/http/RestWrapper";
import {switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";

export class OpmlService {
    upload(opml: File): Observable<boolean> {
        const data = new FormData();
        data.append('opml', opml);
        return rest.post('/opml/import', data).pipe(
            switchMap(response => {
                if (response.ok) {
                    return of(true);
                } else {
                    throw new HttpStatusError(response.status, 'Error while updating feed.');
                }
            }),
            take(1)
        );
    }
}

export default new OpmlService();