import { fr_FR as admin_fr_FR } from '@/locales/admin_fr-FR';

export const fr_FR = {
    ...admin_fr_FR,
    'admin.feeds.confirm.feedsDeletion': 'Supprimer le flux "{feed}" de la base de données définitivement ?' +
        '| Supprimer les {n} flux sélectionnés de la base de données ?',
    'admin.feeds.info.deletionMustContainsOneID': 'Vous devez sélectionner au moins un flux à supprimer !',
    'admin.feeds.messages.feedDeletedSuccessfully': 'Flux {feed} supprimé avec succès ! ' +
        '| {n} flux supprimés avec succès !',
    'admin.feeds.messages.feedDeletionFailed': 'Une erreur s’est produite lors de la suppression de flux !',
    'admin.feeds.tab.feeds.list': 'Liste des flux',
};