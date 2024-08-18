import { smarttable_fr_FR } from '@/locales/smarttable_fr-FR';

const config = await import('@/locales/config_fr-FR');
export const fr_FR = {
    ...smarttable_fr_FR,
    ...config.fr_FR,
    'config.feeds.table.headers': 'nom / lien / catégories / actions',
};
