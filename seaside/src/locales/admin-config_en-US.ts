import { en_US as admin_en_US } from '@/locales/admin_en-US';

export const en_US = {
    ...admin_en_US,

    // Page title & description
    'admin.config.mail.title': 'Mail server configuration',
    'admin.config.mail.subtitle': 'Configure the SMTP settings used to send emails.',

    // Sections
    'admin.config.mail.section.connection': 'Connection',
    'admin.config.mail.section.credentials': 'Credentials',
    'admin.config.mail.section.security': 'Security',
    'admin.config.mail.section.misc': 'Sender & polling',

    // Fields
    'admin.config.mail.smtp.host': 'SMTP host',
    'admin.config.mail.smtp.port': 'SMTP port',
    'admin.config.mail.smtp.secure': 'Use secure connection (STARTTLS/SSL)',
    'admin.config.mail.smtp.username': 'SMTP username',
    'admin.config.mail.smtp.password': 'SMTP password', //NOSONAR
    'admin.config.mail.smtp.ssl.protocols': 'SSL/TLS protocols',
    'admin.config.mail.smtp.ssl.protocols.help':
        'Space-separated list of allowed protocols (e.g. TLSv1.3 TLSv1.2)',
    'admin.config.mail.smtp.requireTls': 'Require TLS',
    'admin.config.mail.smtp.ssl.checkserveridentity': 'Check server identity',
    'admin.config.mail.smtp.from': 'From address',
    'admin.config.mail.smtp.pollingIntervalSeconds': 'Polling interval (seconds)',

    // Actions & messages
    'admin.config.mail.messages.loadingError': 'Error while loading mail configuration.',
    'admin.config.mail.messages.formValidationError': 'Some fields are invalid. Please check the form below.',
    'admin.config.mail.messages.updateSuccess': 'Mail configuration saved successfully.',
    'admin.config.mail.messages.updateError': 'Unable to save mail configuration.',
};