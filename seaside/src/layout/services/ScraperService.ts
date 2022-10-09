import {Observable, throwError} from "rxjs";
import gql from '@/common/services/GraphqlWrapper';
import {map, take} from "rxjs/operators";

const URL_PATTERN = new RegExp('^(https?:\\/\\/)?' + // protocol
    '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|' + // domain name
    '((\\d{1,3}\\.){3}\\d{1,3}))' + // OR ip (v4) address
    '(\\:\\d+)?(\\/[-a-z\\d%_.~+@]*)*' + // port and path
    '(\\?[;&a-z\\d%_.~+=-]*)?' + // query string
    '(\\#[-a-z\\d_]*)?$', 'i'); // fragment locator

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

        return gql.send(ScraperService.SCRAP_SINGLE_NEWS_REQUEST, {newsLink: link}).pipe(
            take(1),
            map(() => undefined)
        );
    }
}

export default new ScraperService();