type ScrapingError = {
    since: Date;
    message: string;
}

export type Feed = {
    _id: string;
    name: string;
    description: string;
    location: string;
    tags: string[];
    icon?: URL;
    error: ScrapingError;
}