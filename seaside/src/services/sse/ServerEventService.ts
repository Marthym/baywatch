import {EventType} from "@/services/sse/EventType.enum";

class ServerEventService {
    private source: EventSource | null = null;

    private listeners: EventListener[] = [];

    public registerListener(eventType: string, listener: EventListener): void {
        if (!this.source) {
            this.source = new EventSource('/api/sse');
            this.source.addEventListener(EventType.OPEN, () => console.debug('Connected to SSE.'))
        }
        this.source.addEventListener(eventType, listener);
    }

    public unregister(eventType: string, listener: EventListener): void {
        if (this.source) {
            this.source.removeEventListener(eventType, listener);
            const idx = this.listeners.indexOf(listener);
            this.listeners.splice(idx);
            if (this.listeners.length <= 1) {
                this.close();
            }
        }
    }

    public close(): void {
        if (!this.source) {
            return;
        }
        this.source.close();
        this.source = null;
    }
}

export default new ServerEventService();