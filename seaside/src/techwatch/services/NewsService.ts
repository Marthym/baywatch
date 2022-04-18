import {EMPTY, from, Observable} from 'rxjs';
import {map, switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {News} from "@/techwatch/model/News";
import {Mark} from "@/techwatch/model/Mark.enum";
import rest from '@/services/http/RestWrapper';
import {ConstantHttpHeaders} from "@/constants";
import {Infinite} from "@/services/model/Infinite";
import {NewsState} from "@/techwatch/model/NewsState";

export class NewsService {

    public static readonly DEFAULT_PER_PAGE: number = 20;
    /**
     * By default query the 10 latest published news
     * @private
     */
    public static readonly DEFAULT_QUERY: string = `?_pp=${NewsService.DEFAULT_PER_PAGE}&_s=-publication`;

    /**
     * Get the {@link News} from backend
     *
     * @param page The to display
     * @param query The possible query parameters
     */
    getNews(page = 0, query: URLSearchParams = new URLSearchParams(NewsService.DEFAULT_QUERY)): Observable<Infinite<News>> {
        if (page > 0) {
            query.append('_p', String(page));
        }
        return rest.get(`/news?${query.toString()}`).pipe(
            map(response => {
                if (response.ok) {
                    const totalCount = parseInt(response.headers.get(ConstantHttpHeaders.X_TOTAL_COUNT) || "-1");
                    const data: Observable<News[]> = from(response.json());
                    return {
                        total: totalCount,
                        data: data
                    };
                } else {
                    throw new HttpStatusError(response.status, `Error while getting news.`);
                }
            }),
            take(1)
        );
    }

    mark(id: string, mark: Mark): Observable<NewsState> {
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

    unmark(id: string, mark: Mark): Observable<NewsState> {
        return rest.put(`/news/${id}/unmark/${mark}`).pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    return EMPTY;
                }
            }),
            take(1)
        );
    }

    private reloadFunction: VoidFunction = () => {
        console.warn('no reload function!')
    };

    /**
     * Register the function call on reload
     * This allows others components to reload news list
     *
     * @param apply [VoidFunction] The call function
     */
    registerReloadFunction(apply: VoidFunction): void {
        this.reloadFunction = apply;
    }

    reload(): void {
        if (this.reloadFunction) {
            this.reloadFunction();
        }
    }
}

export default new NewsService();