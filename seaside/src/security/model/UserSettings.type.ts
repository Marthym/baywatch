import { Locale } from '@/common/model/Locale.type';
import { ViewMode } from '@/common/model/NewsViewMode';

export type UserSettings = {
    preferredLocale: Locale,
    autoread: boolean,
    newsViewMode: ViewMode,
}