import rest from '@/common/services/RestWrapper';
import {catchError, map, take, tap} from "rxjs/operators";
import {Observable, of} from "rxjs";

class ServerEventService {
    private id = Math.random();
    private source: EventSource | null = null;

    private listeners: EventListener[] = [];

    public registerListener(eventType: string, listener: EventListener): void {
        if (!this.source) {
            this.source = new EventSource('/api/sse');
            console.log(`[${this.id}] Create SSE Connection ...`)
            this.source.addEventListener('open', () => console.debug(`[${this.id}] Connected to SSE.`))
        }
        this.source.addEventListener(eventType, listener);
    }

    public unregister(eventType: string, listener: EventListener): void {
        console.log(`[${this.id}] Unregister SSE`, this.source);
        if (this.source) {
            this.source.removeEventListener(eventType, listener);
            const idx = this.listeners.indexOf(listener);
            this.listeners.splice(idx);
            if (this.listeners.length <= 1) {
                this.close();
            }
        }
    }

    public close(): Observable<undefined> {
        if (this.source) {
            console.log(`[${this.id}] Close SSE !`);
            this.source.close();
            this.source = null;
        }
        return rest.delete('/sse').pipe(
            take(1),
            tap(_x => console.debug(`[${this.id}] SSE disconnected.`)),
            catchError(e => {
                console.debug(e.message);
                return of(undefined);
            }),
            map(_x => undefined)
        );
    }
}

export default new ServerEventService();