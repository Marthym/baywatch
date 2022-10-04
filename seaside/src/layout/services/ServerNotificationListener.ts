let id = Math.random();
let source: EventSource | null = null;

let listeners: EventListener[] = [];

export function registerNotificationListener(eventType: string, listener: EventListener): void {
    if (!source) {
        source = new EventSource('/api/sse');
        console.log(`[${id}] Create SSE Connection ...`)
        source.addEventListener('open', () => console.debug(`[${id}] Connected to SSE.`))
    }
    source.addEventListener(eventType, listener);
}
