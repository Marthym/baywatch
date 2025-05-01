export enum Level {
    WARNING = 'WARNING',
    SEVERE = 'SEVERE'
}

export type ScrapingError = {
    level: Level;
    since: string;
    lastTime: string;
    message: string;
}