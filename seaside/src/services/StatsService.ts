import {switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {Statistics} from "@/services/model/Statistics";
import rest from '@/services/http/RestWrapper';

export class StatsService {

    private readonly stats: Statistics = {
        news: 0,
        unread: 0,
        feeds: 0,
        users: 0,
    };

    getBaywatchStats(): Statistics {
        return this.stats;
    }

    incrementUnread(): void {
        ++this.stats.unread;
    }

    decrementUnread(): void {
        --this.stats.unread;
    }

    update(): void {
        rest.get('/stats').pipe(
            switchMap(response => {
                if (response.ok) {
                    return response.json() as Promise<Statistics>;
                } else {
                    throw new HttpStatusError(response.status, `Error while getting news.`);
                }
            }),
            take(1)
        ).subscribe({
            next: s => Object.assign(this.stats, s),
        });
    }
}

export default new StatsService();