import { Locale } from '@/common/model/Locale.type';

export type NewsViewType = 'CARD' | 'MAGAZINE';

export type UserSettings = {
    preferredLocale: Locale,
    autoread: boolean,
    newsView: NewsViewType,
}