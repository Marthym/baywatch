import {from, Observable, throwError} from "rxjs";
import rest from "@/common/services/RestWrapper";
import {map, switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/common/errors/HttpStatusError";

const URL_PATTERN = new RegExp('^(https?:\\/\\/)?' + // protocol
    '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|' + // domain name
    '((\\d{1,3}\\.){3}\\d{1,3}))' + // OR ip (v4) address
    '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*' + // port and path
    '(\\?[;&a-z\\d%_.~+=-]*)?' + // query string
    '(\\#[-a-z\\d_]*)?$', 'i'); // fragment locator

export class ScraperService {
    importStandaloneNews(link?: string): Observable<string> {
        if (link === undefined) {
            return throwError(() => new Error('Link is mandatory !'));
        } else if (!URL_PATTERN.test(link)) {
            return throwError(() => new Error('Argument link must be a valid URL !'));
        }

        const encodedLink = encodeURIComponent(link);
        return rest.post(`/scrap/news/${encodedLink}`).pipe(
            switchMap(response => {
                if (response.ok) {
                    return from(response.json());
                } else {
                    return from(response.json()).pipe(switchMap(j =>
                        throwError(() => new HttpStatusError(response.status, j.message))));
                }
            }),
            take(1),
            map(n => ('_id' in n) ? n._id : n)
        );
    }
}

export default new ScraperService();