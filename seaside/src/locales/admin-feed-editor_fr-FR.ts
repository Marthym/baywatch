import { fr_FR as admin_fr_FR } from '@/locales/admin_fr-FR';
import { backend_scraping_fr_FR } from '@/locales/backend/scraping_fr-FR';

export const fr_FR = {
    ...admin_fr_FR,
    ...backend_scraping_fr_FR,
    'admin.feed.editor.description': 'Description',
    'admin.feed.editor.description.placeholder': 'Saisir une description courte',
    'admin.feed.editor.errors': 'Erreurs',
    'admin.feed.editor.errors.last': 'Dernière fois :',
    'admin.feed.editor.errors.since': 'Depuis :',
    'admin.feed.editor.icon': 'icône du flux',
    'admin.feed.editor.icon.placeholder': 'URL de l’icône du flux: https://...',
    'admin.feed.editor.location': 'Location',
    'admin.feed.editor.location.placeholder': 'URL du flux: https://...',
    'admin.feed.editor.name': 'Nom du flux',
    'admin.feed.editor.name.placeholder': 'Saisir le nom du flux',
    'admin.feed.editor.title': 'Mise à jour de flux',
    'admin.feeds.messages.feedUpdateFailed': 'Erreur lors de la mise à jour du flux !',
    'admin.feeds.messages.feedUpdatedSuccessfully': 'Flux mis à jour avec success!',
};