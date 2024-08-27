import { switchMap, take } from 'rxjs/operators';
import { HttpStatusError } from '@/common/errors/HttpStatusError';
import { from, Observable } from 'rxjs';
import rest from '@/common/services/RestWrapper';

export function tagsListAll(): Observable<string[]> {
    return rest.get('/tags').pipe(
        switchMap(response => {
            if (response.ok) {
                const data: Observable<string[]> = from(response.json());
                return data;
            } else {
                throw new HttpStatusError(response.status, `Error while getting tags.`);
            }
        }),
        take(1),
    );
}
