import { fr_FR as admin_fr_FR } from '@/locales/admin_fr-FR';

export const fr_FR = {
    ...admin_fr_FR,

    // Titre & description
    'admin.config.mail.title': 'Configuration du serveur de mail',
    'admin.config.mail.subtitle': 'Configurez les paramètres SMTP utilisés pour l’envoi des emails.',

    // Sections
    'admin.config.mail.section.connection': 'Connexion',
    'admin.config.mail.section.credentials': 'Identifiants',
    'admin.config.mail.section.security': 'Sécurité',
    'admin.config.mail.section.misc': 'Expéditeur et polling',

    // Champs
    'admin.config.mail.smtp.host': 'hôte SMTP',
    'admin.config.mail.smtp.port': 'port SMTP',
    'admin.config.mail.smtp.secure': 'utiliser une connexion sécurisée (STARTTLS/SSL)',
    'admin.config.mail.smtp.username': 'nom d’utilisateur SMTP',
    'admin.config.mail.smtp.password': 'mot de passe SMTP', //NOSONAR
    'admin.config.mail.smtp.ssl.protocols': 'protocoles SSL/TLS',
    'admin.config.mail.smtp.ssl.protocols.help':
        'Liste des protocoles autorisés séparés par des espaces (ex. TLSv1.3 TLSv1.2)',
    'admin.config.mail.smtp.requireTls': 'exiger TLS',
    'admin.config.mail.smtp.ssl.checkserveridentity': 'vérifier l’identité du serveur',
    'admin.config.mail.smtp.from': 'adresse d’expéditeur',
    'admin.config.mail.smtp.pollingIntervalSeconds': 'intervalle de polling (secondes)',

    // Actions & messages
    'admin.config.mail.messages.loadingError': 'Erreur lors du chargement de la configuration du serveur mail.',
    'admin.config.mail.messages.formValidationError': 'Certains champs contiennent des erreurs, merci de vérifier votre saisie.',
    'admin.config.mail.messages.updateSuccess': 'Configuration du serveur mail enregistrée avec succès.',
    'admin.config.mail.messages.updateError': 'Erreur lors de l’enregistrement de la configuration du serveur mail.',
};