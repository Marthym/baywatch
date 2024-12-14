import { smarttable_fr_FR } from '@/locales/components/smarttable_fr-FR';
import { taginput_fr_FR } from '@/locales/components/taginput_fr-FR';
import { fileupload_fr_FR } from '@/locales/components/fileuploadwindow_fr-FR';
import { fr_FR as config_fr_FR } from '@/locales/config_fr-FR';
import { backend_scraping_fr_FR } from '@/locales/backend/scraping_fr-FR';

export const fr_FR = {
    ...taginput_fr_FR,
    ...smarttable_fr_FR,
    ...fileupload_fr_FR,
    ...config_fr_FR,
    ...backend_scraping_fr_FR,
    'config.feeds.table.headers': 'nom / location / catégories / actions',
    'config.feeds.editor.title': 'ajouter un flux',
    'config.feeds.editor.form.location': 'location',
    'config.feeds.editor.form.name': 'nom du flux',
    'config.feeds.editor.form.name.placeholder': 'nom du flux',
    'config.feeds.editor.form.description': 'description',
    'config.feeds.editor.form.action.submit': 'enregistrer',
    'config.feeds.messages.nameMandatory': 'Le nom du flux est obligatoire',
    'config.feeds.messages.locationMustBeURL': 'La location du flux doit être une URL',
    'config.feeds.messages.feedSubscribedSuccessfully': 'Flux souscrit avec succès !',
    'config.feeds.messages.unableEditElement': 'Erreur lors de l’édition de l’élément {idx} !',
    'config.feeds.messages.feedUpdatedSuccessfully': 'Flux mis à jour avec succès !',
    'config.feeds.messages.feedUpdatedFailed': 'Erreur lors de la mise à jour du flux !',
    'config.feeds.messages.feedUnsubscribedSuccessfully': 'Flux {feed} supprimé avec succès ! ' +
        '| {n} flux supprimés avec succès !',
    'config.feeds.messages.feedUnsubscribedFailed': 'Une erreur s’est produite lors de la suppression de flux !',
    'config.feeds.confirm.feedsDeletion': 'Supprimer le flux "<b>{feed}</b>" de vos souscriptions ? ' +
        '| Supprimer les {n} flux sélectionnés ?',
    'config.feeds.messages.opmlLoadedSuccessfully': 'Fichier OPML importé avec succès.',
    'config.feeds.messages.opmlLoadedFailed': 'Erreur lors de l’import du fichier OPML !',
};
