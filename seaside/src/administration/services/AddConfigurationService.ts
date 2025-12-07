import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { send } from '@/common/services/GraphQLClient';
import { MailSmtpConfig } from '@/administration/model/MailSmtpConfig.type';

const ADMIN_APP_CONFIGURATION_MAIL_SMTP = `#graphql
query AdminAppConfigurationMailSmtp {
    adminAppConfigurationMailSmtp {
        from host port pollingIntervalSeconds
        requireTls secure sslCheckserveridentity
        sslProtocols username
    }
}`;

export function adminAppConfigurationMailSmtp(): Observable<MailSmtpConfig> {
    return send<{ adminAppConfigurationMailSmtp: MailSmtpConfig }>(ADMIN_APP_CONFIGURATION_MAIL_SMTP).pipe(
        map(res => res.data.adminAppConfigurationMailSmtp),
        take(1),
    );
}
