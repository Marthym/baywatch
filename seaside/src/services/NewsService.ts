import {Observable} from 'rxjs';
import {switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {News} from "@/services/model/News";
import {Mark} from "@/services/model/Mark.enum";
import rest from '@/services/http/RestWrapper';

export class NewsService {
    /**
     * By default query the 10 latest published news
     * @private
     */
    public static readonly DEFAULT_QUERY: string = '?_pp=20&_s=-publication';

    /**
     * Get the {@link News} from backend
     *
     * @param page The to display
     * @param query The possible query parameters
     */
    getNews(page = 1, query: URLSearchParams = new URLSearchParams(NewsService.DEFAULT_QUERY)): Observable<News[]> {
        if (page > 1) {
            query.append('_p', String(page));
        }
        return rest.get(`/news?${query.toString()}`).pipe(
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

    mark(id: string, mark: Mark): Observable<News> {
        return rest.put(`/news/${id}/mark/${mark}`).pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new HttpStatusError(response.status, `Error while mark news ${id} as ${mark}.`);
                }
            }),
            take(1)
        );
    }

    unmark(id: string, mark: Mark): Observable<News> {
        return rest.put(`/news/${id}/unmark/${mark}`).pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new HttpStatusError(response.status, `Error while unmark news ${id} as ${mark}.`);
                }
            }),
            take(1)
        );
    }
}

export default new NewsService();