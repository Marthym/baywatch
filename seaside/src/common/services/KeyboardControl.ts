class KeyboardControl {

    private listeners: Map<string, (event: KeyboardEvent) => void> = new Map();
    private listener = event => this.onKeyDownListener(event);

    registerListener(key: string, consumer: (event: KeyboardEvent) => void): KeyboardControl {
        this.listeners.set(key, consumer);
        return this;
    }

    unregisterListener(...keys: string[]): KeyboardControl {
        keys.forEach(k => this.listeners.delete(k));
        return this;
    }

    startKeyboardControl(): void {
        window.addEventListener('keydown', this.listener, false);
    }

    stopKeyboardControl(): void {
        window.removeEventListener('keydown', this.listener, false);
    }

    private onKeyDownListener(event: KeyboardEvent): void {
        if (!this.listeners || event.altKey) {
            return;
        }
        if (!event.altKey && this.listeners.has(event.key)) {
            const consumer = this.listeners.get(event.key);
            if (consumer) {
                consumer(event);
            }
        }
    }
}

export default new KeyboardControl();
