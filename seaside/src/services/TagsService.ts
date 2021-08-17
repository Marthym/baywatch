import {switchMap, take} from "rxjs/operators";
import {HttpStatusError} from "@/services/model/exceptions/HttpStatusError";
import {from, Observable} from "rxjs";
import rest from '@/services/http/RestWrapper';

export class TagsService {
    private listenerIdx = 0;
    private tagListeners: Map<number, ((t: string) => void)> = new Map();

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

    registerListener(callback: ((t: string) => void)): number {
        this.tagListeners.set(++this.listenerIdx, callback);
        return this.listenerIdx;
    }

    unregisterListener(idx: number): void {
        if (idx <= 0) return;
        this.tagListeners.delete(idx);
    }

    select(tag: string): string {
        this.tagListeners.forEach(callback => callback(tag));
        return tag;
    }
}

export default new TagsService();