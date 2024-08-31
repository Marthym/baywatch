import { fr_FR as admin_fr_FR } from '@/locales/admin_fr-FR';

export const fr_FR = {
    ...admin_fr_FR,
    'admin.users.add': 'ajouter',
    'admin.users.import': 'importer',
    'admin.users.export': 'exporter',
    'admin.users.delete': 'supprimer',
    'admin.users.login': 'pseudo',
    'admin.users.username': 'nom',
    'admin.users.mail': 'mail',
    'admin.users.role': 'rôle',
    'admin.users.createdAt': 'créé le',
    'admin.users.lastActivity': 'dernière activité',
    'admin.users.messages.userIdCopied': 'Identifiant copié dans le presse papier !',
    'admin.users.messages.userCreatedSuccessfully': 'Utilisateur {login} créé avec succès',
    'admin.users.messages.userUpdatedSuccessfully': 'Utilisateur {login} modifié avec succès',
    'admin.users.messages.userDeletedSuccessfully': 'Utilisateur {login} supprimé avec succès ! ' +
        '| Tous les utilisateurs supprimés avec succès !',
    'admin.users.messages.unableDeleteUser': 'Impossible de supprimer l’utilisateur {login} ! ' +
        '| Impossible de supprimer les utilisateurs sélectionnés !',
    'admin.users.messages.configUsersDeletion': 'NOP ' +
        '| Etes-vous certain de vouloir supprimer l’utilisateur {login} ? ' +
        '| Etes-vous certain de vouloir supprimer {count} utilisateurs ?',
};