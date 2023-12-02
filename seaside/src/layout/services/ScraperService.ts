import { Observable, throwError } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { send } from '@/common/services/GraphQLClient';
import { URL_PATTERN } from '@/common/services/RegexPattern';

export class ScraperService {
    private static readonly SCRAP_SINGLE_NEWS_REQUEST = `#graphql
    mutation ScrapSingleNews($newsLink: URI!) {
        scrapSimpleNews(uri: $newsLink)
    }`;

    importStandaloneNews(link?: string): Observable<void> {
        if (link === undefined) {
            return throwError(() => new Error('Link is mandatory !'));
        } else if (!URL_PATTERN.test(link)) {
            return throwError(() => new Error('Argument link must be a valid URL !'));
        }

        return send(ScraperService.SCRAP_SINGLE_NEWS_REQUEST, { newsLink: link }).pipe(
            take(1),
            map(() => undefined),
        );
    }
}

export default new ScraperService();