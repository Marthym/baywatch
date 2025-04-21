import { ScrapingError } from '@/administration/model/ScraperError';

export type RawFeed = {
    _id: string
    name: string
    url: string
    icon: string
    description: string
    lastETag: string
    lastWatch: Date
    error: ScrapingError
}