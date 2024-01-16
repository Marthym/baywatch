import { Ref, UnwrapRef } from '@vue/reactivity';

type KeyboardListener = {
    key: string,
    consumer: (event: KeyboardEvent) => void,
};

type OnKeydownCallback = (event: KeyboardEvent) => void;

export type KeyboardController = {
    listeners: Map<string, KeyboardListener>,
    root: Ref<UnwrapRef<HTMLElement>>,
    callback?: OnKeydownCallback,
    register: (...toRegister: KeyboardListener[]) => this
    start: () => void;
    stop: () => void;
    purge: () => void;
};

export function useKeyboardController(root: Ref<UnwrapRef<HTMLElement>>): KeyboardController {
    return {
        listeners: new Map(),
        root: root,
        register: function (...toRegister: KeyboardListener[]) {
            toRegister.forEach(kbl => this.listeners.set(kbl.key, kbl));
            return this;
        },
        start: function () {
            startController(this);
        },
        stop: function () {
            stopController(this);
        },
        purge: function () {
            purgeController(this);
        },
    };
}

export function listener(key: string, consumer: (event: KeyboardEvent) => void): KeyboardListener {
    return { key, consumer };
}

function startController(controller: KeyboardController): void {
    controller.callback = (event: KeyboardEvent) => onKeyDownListener(controller.listeners, event);
    controller.root.value.addEventListener('keydown', controller.callback, false);
    controller.root.value.tabIndex = -1;
    controller.root.value.focus();
}

function stopController(controller: KeyboardController): void {
    if (controller.callback) {
        controller.root.value.removeEventListener('keydown', controller.callback, false);
    }
}

function purgeController(controller: KeyboardController): void {
    stopController(controller);
    for (let key of controller.listeners.keys()) {
        controller.listeners.delete(key);
    }
}

function onKeyDownListener(listeners: Map<string, KeyboardListener>, event: KeyboardEvent): void {
    const targetType: string = (event.target as HTMLInputElement).type?.toLowerCase() || 'accepted';
    if (!listeners || event.altKey ||
        (['text', 'password', 'textarea', 'email'].includes(targetType) && event.key !== 'Escape')) {
        return;
    }
    if (!event.altKey && listeners.has(event.key)) {
        const listener = listeners.get(event.key);
        if (listener) {
            listener.consumer(event);
        }
    }
}