export type Counter = {
    name: string,
    value: string,
    description: string,
    icon?: string
};

export const NONE: Counter = {
    name: 'loading...',
    value: 'loading...',
    description: 'loading...'
};
