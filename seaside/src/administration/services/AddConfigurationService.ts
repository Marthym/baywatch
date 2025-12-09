import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { send } from '@/common/services/GraphQLClient';
import { MailSmtpConfig } from '@/administration/model/MailSmtpConfig.type';

type AdminAppConfigurationMailSmtpResponse = { adminAppConfigurationMailSmtp: { mail: { smtp: MailSmtpConfig } } };

const ADMIN_APP_CONFIGURATION_MAIL_SMTP = `#graphql
query AdminAppConfigurationMailSmtp {
    adminAppConfigurationMailSmtp {
        mail {smtp {
            from host port pollingIntervalSeconds
            requireTls secure username ssl {
                protocols checkserveridentity
            }
        }}
    }
}`;

export function adminAppConfigurationMailSmtp(): Observable<MailSmtpConfig> {
    return send<AdminAppConfigurationMailSmtpResponse>(ADMIN_APP_CONFIGURATION_MAIL_SMTP).pipe(
        map(res => res.data.adminAppConfigurationMailSmtp.mail.smtp),
        take(1),
    );
}
