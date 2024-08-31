import { smarttable_fr_FR } from '@/locales/components/smarttable_fr-FR';
import { taginput_fr_FR } from '@/locales/components/taginput_fr-FR';
import { fileupload_fr_FR } from '@/locales/components/fileuploadwindow_fr-FR';
import { fr_FR as config_fr_FR } from '@/locales/config_fr-FR';

export const fr_FR = {
    ...taginput_fr_FR,
    ...smarttable_fr_FR,
    ...fileupload_fr_FR,
    ...config_fr_FR,
    'config.feeds.table.headers': 'nom / location / catégories / actions',
    'config.feeds.editor.title': 'ajouter un flux',
    'config.feeds.editor.form.location': 'location',
    'config.feeds.editor.form.name': 'nom du flux',
    'config.feeds.editor.form.name.placeholder': 'nom du flux',
    'config.feeds.editor.form.description': 'description',
    'config.feeds.editor.form.action.submit': 'ajouter',
};