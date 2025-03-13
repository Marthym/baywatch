let source: EventSource | null = null;

let listeners: EventListener[] = [];

export function registerNotificationListener(eventType: string, listener: EventListener): void {
    if (!source) {
        source = new EventSource('/api/sse', { withCredentials: true });
        console.debug(`Create SSE Connection ...`);
        source.addEventListener('open', () => console.debug(`Connected to SSE.`));
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

export function closeNotificationListeners(): void {
    listeners.splice(0, listeners.length);
    if (source) {
        console.debug(`Try closing SSE ...`);
        source.addEventListener('close', () => console.info('SSE Connection closed'));
        source.close();
        source = null;
    }
}