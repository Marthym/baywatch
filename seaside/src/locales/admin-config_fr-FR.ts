import { fr_FR as admin_fr_FR } from '@/locales/admin_fr-FR';

export const fr_FR = {
    ...admin_fr_FR,

    // Titre & description
    'config.admin.mail.title': 'Configuration du serveur de mail',
    'config.admin.mail.subtitle': 'Configurez les paramètres SMTP utilisés pour l’envoi des emails.',

    // Sections
    'config.admin.mail.section.connection': 'Connexion',
    'config.admin.mail.section.credentials': 'Identifiants',
    'config.admin.mail.section.security': 'Sécurité',
    'config.admin.mail.section.misc': 'Expéditeur et polling',

    // Champs
    'config.admin.mail.smtp.host': 'hôte SMTP',
    'config.admin.mail.smtp.port': 'port SMTP',
    'config.admin.mail.smtp.secure': 'utiliser une connexion sécurisée (STARTTLS/SSL)',
    'config.admin.mail.smtp.username': 'nom d’utilisateur SMTP',
    'config.admin.mail.smtp.password': 'mot de passe SMTP', //NOSONAR
    'config.admin.mail.smtp.ssl.protocols': 'protocoles SSL/TLS',
    'config.admin.mail.smtp.ssl.protocols.help':
        'Liste des protocoles autorisés séparés par des espaces (ex. TLSv1.3 TLSv1.2)',
    'config.admin.mail.smtp.requireTls': 'exiger TLS',
    'config.admin.mail.smtp.ssl.checkserveridentity': 'vérifier l’identité du serveur',
    'config.admin.mail.smtp.from': 'adresse d’expéditeur',
    'config.admin.mail.smtp.pollingIntervalSeconds': 'intervalle de polling (secondes)',

    // Actions & messages
    'config.admin.mail.action.save': 'enregistrer la configuration',
    'config.admin.mail.messages.updateSuccess':
        'Configuration du serveur mail enregistrée avec succès.',
    'config.admin.mail.messages.updateError':
        'Erreur lors de l’enregistrement de la configuration du serveur mail.',
};