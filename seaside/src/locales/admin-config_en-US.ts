import { en_US as admin_en_US } from '@/locales/admin_en-US';

export const en_US = {
    ...admin_en_US,

    // Page title & description
    'config.admin.mail.title': 'Mail server configuration',
    'config.admin.mail.subtitle': 'Configure the SMTP settings used to send emails.',

    // Sections
    'config.admin.mail.section.connection': 'Connection',
    'config.admin.mail.section.credentials': 'Credentials',
    'config.admin.mail.section.security': 'Security',
    'config.admin.mail.section.misc': 'Sender & polling',

    // Fields
    'config.admin.mail.smtp.host': 'SMTP host',
    'config.admin.mail.smtp.port': 'SMTP port',
    'config.admin.mail.smtp.secure': 'Use secure connection (STARTTLS/SSL)',
    'config.admin.mail.smtp.username': 'SMTP username',
    'config.admin.mail.smtp.password': 'SMTP password',
    'config.admin.mail.smtp.ssl.protocols': 'SSL/TLS protocols',
    'config.admin.mail.smtp.ssl.protocols.help':
        'Space-separated list of allowed protocols (e.g. TLSv1.3 TLSv1.2)',
    'config.admin.mail.smtp.requireTls': 'Require TLS',
    'config.admin.mail.smtp.ssl.checkserveridentity': 'Check server identity',
    'config.admin.mail.smtp.from': 'From address',
    'config.admin.mail.smtp.pollingIntervalSeconds': 'Polling interval (seconds)',

    // Actions & messages
    'config.admin.mail.action.save': 'Save configuration',
    'config.admin.mail.messages.updateSuccess': 'Mail configuration saved successfully.',
    'config.admin.mail.messages.updateError': 'Unable to save mail configuration.',
};