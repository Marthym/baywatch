import { smarttable_en_US } from '@/locales/components/smarttable_en-US';
import { taginput_en_US } from '@/locales/components/taginput_en-US';
import { fileupload_en_US } from '@/locales/components/fileuploadwindow_en-US';
import { en_US as config_en_US } from './config_en-US';

export const en_US = {
    ...taginput_en_US,
    ...smarttable_en_US,
    ...fileupload_en_US,
    ...config_en_US,
    'config.feeds.table.headers': 'name / link / categories / actions',
    'config.feeds.editor.title': 'add new feed',
    'config.feeds.editor.form.location': 'location',
    'config.feeds.editor.form.name': 'name',
    'config.feeds.editor.form.name.placeholder': 'name',
    'config.feeds.editor.form.description': 'description',
    'config.feeds.editor.form.action.submit': 'save',
    'config.feeds.messages.feedUnsubscribeSuccessfully': 'Flux {feed} supprimé avec succès ! ' +
        '| {n} flux supprimés avec succès !',
    'config.feeds.messages.feedUnsubscribeFailed': 'An error occur during feed unsubscribe !',
    'config.feeds.confirm.feedsDeletion': 'Unsubscribe from "<b>{feed}</b>" ?' +
        '| Unsubscribe from {n} feeds ?',
};
