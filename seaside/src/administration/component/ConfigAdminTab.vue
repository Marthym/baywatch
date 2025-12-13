<template>
  <section class="max-w-3xl [&_input]:text-base-content [&_label]:first-letter:uppercase">
    <h2 class="card-title first-letter:uppercase">
      {{ t('admin.config.mail.title') || 'Mail server configuration' }}
    </h2>
    <p class="text-sm text-base-content/70">
      {{ t('admin.config.mail.subtitle') || 'Configure SMTP settings used to send emails.' }}
    </p>

    <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
      <legend class="fieldset-legend">{{ t('admin.config.mail.section.connection') || 'Connection' }}</legend>

      <label class="label block">{{ t('admin.config.mail.smtp.host') || 'SMTP Host' }}
        <input v-model="mailConfig.host"
               :class="{'input-error': errors.includes('host')}"
               autocomplete="off"
               class="input input-bordered w-full block"
               @input="clearError('host')"
               type="text"/>
      </label>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="label block">{{ t('admin.config.mail.smtp.port') || 'SMTP Port' }}
          <input v-model.number="mailConfig.port"
                 :class="{'input-error': errors.includes('port')}"
                 class="input input-bordered w-full block"
                 @input="clearError('port')"
                 max="65535" min="1" type="number"/>
        </label>

        <label class="label mt-4">{{ t('admin.config.mail.smtp.secure') || 'Use STARTTLS/secure connection' }}
          <input v-model="mailConfig.secure"
                 :class="{'toggle-error': errors.includes('secure')}"
                 class="toggle ml-2"
                 @change="clearError('secure')"
                 type="checkbox"/>
        </label>
      </div>
    </fieldset>

    <!-- Credentials -->
    <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
      <legend class="fieldset-legend">{{ t('admin.config.mail.section.credentials') || 'Credentials' }}</legend>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="label block">{{ t('admin.config.mail.smtp.username') || 'SMTP Username' }}
          <input v-model="mailConfig.username"
                 :class="{'input-error': errors.includes('username')}"
                 autocomplete="off"
                 class="input input-bordered w-full block"
                 @input="clearError('username')"
                 type="text"/>
        </label>
        <label class="label block">{{ t('admin.config.mail.smtp.password') || 'SMTP Password' }}
          <input v-model="mailConfig.password"
                 :class="{'input-error': errors.includes('password')}"
                 autocomplete="off"
                 class="input input-bordered w-full block"
                 @input="clearError('password')"
                 type="password"/>
        </label>
      </div>
    </fieldset>

    <!-- TLS / SSL -->
    <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
      <legend class="fieldset-legend">{{ t('admin.config.mail.section.security') || 'Security' }}</legend>

      <label class="label block">{{ t('admin.config.mail.smtp.ssl.protocols') || 'SSL/TLS Protocols' }}
        <input v-model="mailConfig.ssl.protocols"
               :class="{'input-error': errors.includes('ssl.protocols')}"
               class="input input-bordered w-full block"
               @input="clearError('ssl.protocols')"
               placeholder="TLSv1.3 TLSv1.2"
               type="text"/>
        <span class="label text-wrap">{{
            t('admin.config.mail.smtp.ssl.protocols.help') || 'Space-separated list of protocols'
          }}</span>
      </label>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="label block">{{ t('admin.config.mail.smtp.requireTls') || 'Require TLS' }}
          <input v-model="mailConfig.requireTls"
                 :class="{'toggle-error': errors.includes('requireTls')}"
                 class="toggle ml-2"
                 @change="clearError('requireTls')"
                 type="checkbox"/>
        </label>
        <label class="label block">{{ t('admin.config.mail.smtp.ssl.checkserveridentity') || 'Check server identity' }}
          <input v-model="mailConfig.ssl.checkserveridentity"
                 :class="{'toggle-error': errors.includes('ssl.checkserveridentity')}"
                 class="toggle ml-2"
                 @change="clearError('ssl.checkserveridentity')"
                 type="checkbox"/>
        </label>
      </div>
    </fieldset>

    <!-- Sender & Polling -->
    <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
      <legend class="fieldset-legend">{{ t('admin.config.mail.section.misc') || 'Sender & Polling' }}</legend>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="label block">{{ t('admin.config.mail.smtp.from') || 'From address' }}
          <input v-model="mailConfig.from"
                 :class="{'input-error': errors.includes('from')}"
                 autocomplete="off"
                 class="input input-bordered w-full block"
                 @input="clearError('from')"
                 type="email"/>
        </label>
        <label class="label block">{{
            t('admin.config.mail.smtp.pollingIntervalSeconds') || 'Polling interval (seconds)'
          }}
          <input v-model.number="mailConfig.pollingIntervalSeconds"
                 :class="{'input-error': errors.includes('pollingIntervalSeconds')}"
                 class="input input-bordered w-full block"
                 @input="clearError('pollingIntervalSeconds')"
                 min="1"
                 type="number"/>
        </label>
      </div>
    </fieldset>

    <!-- Actions -->
    <div class="card-actions justify-end pt-2">
      <button class="btn block first-letter:uppercase"
              @click.stop="onClickCancel()">
        {{ t('dialog.cancel') || 'cancel' }}
      </button>
      <button class="btn btn-primary block first-letter:uppercase"
              @click.stop="onClickSaveConfig()">
        {{ t('dialog.save') || 'save' }}
      </button>
    </div>
  </section>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { useI18n } from 'vue-i18n';
import { MailSmtpConfig, MailSmtpConfigSchema } from '@/administration/model/MailSmtpConfig.type';
import {
  adminAppConfigurationMailSmtp,
  adminAppConfigurationMailSmtpUpdate,
} from '@/administration/services/AddConfigurationService';
import { isValiError, parse, ValiError } from 'valibot';
import notificationService from '@/services/notification/NotificationService';

@Component({
  name: 'ConfigAdminTab',
  components: {},
  setup() {
    const { t } = useI18n();
    return { t };
  },
})
export default class ConfigAdminTab extends Vue {
  private t!: (key: string) => string;

  private mailConfig: MailSmtpConfig = { ssl: {} } as MailSmtpConfig;
  private readonly errors: string[] = [];

  mounted(): void {
    this.loadMailConfigFromServer();
  }

  private clearError(path: string): void {
    const i = this.errors.indexOf(path);
    if (i !== -1) this.errors.splice(i, 1);
  }

  private loadMailConfigFromServer(): void {
    this.errors.splice(0);
    adminAppConfigurationMailSmtp().subscribe({
      next: (mailConfig) => {
        Object.assign(this.mailConfig, mailConfig);
      },
      error: (error) => {
        notificationService.pushSimpleError(this.t('admin.config.mail.messages.loadingError'));
        console.error(error.message);
      },
    });
  }

  private onClickCancel(): void {
    this.loadMailConfigFromServer();
  }

  private onClickSaveConfig(): void {
    try {
      parse(MailSmtpConfigSchema, this.mailConfig);
      adminAppConfigurationMailSmtpUpdate(this.mailConfig).subscribe({
        next: (mailConfig) => {
          Object.assign(this.mailConfig, mailConfig);
          notificationService.pushSimpleOk(this.t('admin.config.mail.messages.updateSuccess'));
        },
        error: (err) => {
          console.error(err);
          notificationService.pushSimpleError(this.t('admin.config.mail.messages.updateError'));
        },
      });
    } catch (e) {
      if (isValiError(e)) {
        this.handleValiError(e);
      }
      notificationService.pushSimpleError(this.t('admin.config.mail.messages.formValidationError'));
    }
  }

  private handleValiError(error: ValiError<typeof MailSmtpConfigSchema>): void {
    this.errors.splice(0);
    error.issues.forEach((value) => {
      if (value.path) {
        this.errors.push(value.path.map((p: { key: any }) => p.key).join('.'));
      } else {
        console.debug('No path for validation error: ', value);
      }
    });
  }
}
</script>