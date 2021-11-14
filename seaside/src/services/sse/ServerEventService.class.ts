import {ServerEventListener} from "@/services/sse/ServerEventListener.interface";

class ServerEventService {
    private source: EventSource;

    private listeners: ServerEventListener[] = [];

    public registerListener(eventType: string, listener: ServerEventListener): void {
        if (!this.source) {
            this.source = new EventSource('/api/sse');
            this.source.addEventListener('open', event => console.debug('Connected to SSE.'))
        }
        this.source.addEventListener(eventType, event => listener.onEvent(event));
    }

    public unregister(eventType: string, listener: ServerEventListener): void {
        this.source?.removeEventListener(eventType, listener)
    }

    public close(): void {
        this.source?.close();
    }
}

export default new ServerEventService();