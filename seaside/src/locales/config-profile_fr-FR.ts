import { smarttable_fr_FR } from '@/locales/smarttable_fr-FR';

const config = await import('@/locales/config_fr-FR');
export const fr_FR = {
    ...smarttable_fr_FR,
    ...config.fr_FR,
    'config.profile.form.id': 'id',
    'config.profile.form.login': 'identifiant',
    'config.profile.form.name': 'Nom',
    'config.profile.form.mail': 'adresse mail',
    'config.profile.form.password': 'changer',
    'config.profile.form.password.placeholder': 'Nécessaire pour mettre à jour votre profil',
    'config.profile.form.password.tooltip': 'Cliquez pour changer votre mot de passe',
    'config.profile.form.action.save': 'enregistrer',
    'config.profile.avatar.notice': 'l’avatar est téléchargé depuis Gravatar selon votre adresse mail',
    'config.profile.mail.notice': 'l’adresse mail n’est utilisée que pour récupérer l’avatar',
    'config.profile.messages.unableToUpdate': 'Erreur lors de la sauvegarde de votre profil !',
    'config.profile.messages.profileUpdateSuccessfully': 'Profil sauvegarder avec succès !',
    'config.password.title': 'Changement de mot de passe',
    'config.password.form.old.placeholder': 'mot de passe actuel',
    'config.password.form.new.placeholder': 'nouveau mot de passe',
    'config.password.form.confirm.placeholder': 'confirmation du mot de passe',
    'config.password.form.action.submit': 'mettre à jour',
    'config.password.error.old.mandatory': 'Vous devez renseigner votre mot de passe actuel',
    'config.password.error.new.unsecure': 'Mot de passe trop faible. Il peut être piraté instantanément !',
    'config.password.error.confirm.different': 'Confirmation est différente du nouveau mot de passe !',
};
