import {Observable, of} from "rxjs";
import rest from "@/common/services/RestWrapper";
import {catchError, map, take, tap} from "rxjs/operators";

let source: EventSource | null = null;

let listeners: EventListener[] = [];

export function registerNotificationListener(eventType: string, listener: EventListener): void {
    if (!source) {
        source = new EventSource('/api/sse', {withCredentials: true});
        console.info(`Create SSE Connection ...`)
        source.addEventListener('open', () => console.debug(`Connected to SSE.`))
    }
    source.addEventListener(eventType, listener);
}

export function unregisterNotificationListener(eventType: string, listener: EventListener): void {
    console.info(`Unregister SSE listener`, source);
    if (source) {
        source.removeEventListener(eventType, listener);
        const idx = listeners.indexOf(listener);
        listeners.splice(idx);
        if (listeners.length <= 1) {
            close();
        }
    }
}

export function closeNotificationListeners(): Observable<undefined> {
    listeners.splice(0, listeners.length);
    if (source) {
        console.info(`Close SSE !`);
        source.close();
        source = null;
    }
    return rest.delete('/sse').pipe(
        take(1),
        tap(_x => console.debug(`SSE disconnected.`)),
        catchError(e => {
            console.debug(e.message);
            return of(undefined);
        }),
        map(_x => undefined)
    );
}