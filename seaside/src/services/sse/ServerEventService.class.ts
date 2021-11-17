class ServerEventService {
    private source: EventSource = new EventSource('/api/sse');

    private listeners: EventListener[] = [];

    public registerListener(eventType: string, listener: EventListener): void {
        if (!this.source) {
            this.source = new EventSource('/api/sse');
            this.source.addEventListener('open', () => console.debug('Connected to SSE.'))
        }
        this.source.addEventListener(eventType, listener);
    }

    public unregister(eventType: string, listener: EventListener): void {
        this.source.removeEventListener(eventType, listener);
        const idx = this.listeners.indexOf(listener);
        this.listeners.splice(idx);
        if (this.listeners.length <= 1) {
            this.close();
        }
    }

    public close(): void {
        this.source.close();
    }
}

export default new ServerEventService();