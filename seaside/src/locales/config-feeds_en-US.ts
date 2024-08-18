import { smarttable_en_US } from '@/locales/smarttable_en-US';

const config = await import('@/locales/config_en-US');
export const en_US = {
    ...smarttable_en_US,
    ...config.en_US,
    'config.feeds.table.headers': 'name / link / categories / actions',
};
