<template>
  <section class="max-w-3xl">
    <h2 class="card-title capitalize">
      {{ t('config.admin.mail.title') || 'Mail server configuration' }}
    </h2>
    <p class="text-sm text-base-content/70">
      {{ t('config.admin.mail.subtitle') || 'Configure SMTP settings used to send emails.' }}
    </p>

    <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
      <legend class="fieldset-legend">{{ t('config.admin.mail.section.connection') || 'Connection' }}</legend>

      <label class="label block">{{ t('config.admin.mail.smtp.host') || 'SMTP Host' }}
        <input v-model="mailConfig.host"
               autocomplete="off"
               class="input input-bordered w-full block"
               type="text"/>
      </label>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="label block">{{ t('config.admin.mail.smtp.port') || 'SMTP Port' }}
          <input v-model.number="mailConfig.port"
                 class="input input-bordered w-full block"
                 max="65535" min="1" type="number"/>
        </label>

        <label class="label mt-4">{{ t('config.admin.mail.smtp.secure') || 'Use STARTTLS/secure connection' }}
          <input v-model="mailConfig.secure"
                 class="toggle ml-2"
                 type="checkbox"/>
        </label>
      </div>
    </fieldset>

    <!-- Credentials -->
    <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
      <legend class="fieldset-legend">{{ t('config.admin.mail.section.credentials') || 'Credentials' }}</legend>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="label block">{{ t('config.admin.mail.smtp.username') || 'SMTP Username' }}
          <input v-model="mailConfig.username"
                 autocomplete="off"
                 class="input input-bordered w-full block"
                 type="text"/>
        </label>
        <label class="label block">{{ t('config.admin.mail.smtp.password') || 'SMTP Password' }}
          <input v-model="mailConfig.password"
                 autocomplete="off"
                 class="input input-bordered w-full block"
                 type="password"/>
        </label>
      </div>
    </fieldset>

    <!-- TLS / SSL -->
    <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
      <legend class="fieldset-legend">{{ t('config.admin.mail.section.security') || 'Security' }}</legend>

      <label class="label block">{{ t('config.admin.mail.smtp.ssl.protocols') || 'SSL/TLS Protocols' }}
        <input v-model="mailConfig.sslProtocols"
               class="input input-bordered w-full block"
               placeholder="TLSv1.3 TLSv1.2"
               type="text"/>
        <span class="label text-wrap">{{
            t('config.admin.mail.smtp.ssl.protocols.help') || 'Space-separated list of protocols'
          }}</span>
      </label>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="label block">{{ t('config.admin.mail.smtp.requireTls') || 'Require TLS' }}
          <input v-model="mailConfig.requireTls"
                 class="toggle ml-2"
                 type="checkbox"/>
        </label>
        <label class="label block">{{ t('config.admin.mail.smtp.ssl.checkserveridentity') || 'Check server identity' }}
          <input v-model="mailConfig.sslCheckServerIdentity"
                 class="toggle ml-2"
                 type="checkbox"/>
        </label>
      </div>
    </fieldset>

    <!-- Sender & Polling -->
    <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
      <legend class="fieldset-legend">{{ t('config.admin.mail.section.misc') || 'Sender & Polling' }}</legend>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label class="label block">{{ t('config.admin.mail.smtp.from') || 'From address' }}
          <input v-model="mailConfig.from"
                 autocomplete="off"
                 class="input input-bordered w-full block"
                 type="email"/>
        </label>
        <label class="label block">{{
            t('config.admin.mail.smtp.pollingIntervalSeconds') || 'Polling interval (seconds)'
          }}
          <input v-model.number="mailConfig.pollingIntervalSeconds"
                 class="input input-bordered w-full block"
                 min="1"
                 type="number"/>
        </label>
      </div>
    </fieldset>

    <!-- Actions -->
    <div class="card-actions justify-end pt-2">
      <button class="btn btn-primary capitalize"
              @click.stop="onClickSaveConfig()">
        {{ t('config.admin.mail.action.save') || 'Save configuration' }}
      </button>
    </div>
  </section>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import { useI18n } from 'vue-i18n';

type MailConfig = {
  host: string;
  port: number;
  secure: boolean;
  username: string;
  password: string;
  sslProtocols: string;
  requireTls: boolean;
  sslCheckServerIdentity: boolean;
  from: string;
  pollingIntervalSeconds: number;
};

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

  private mailConfig: MailConfig = {
    host: 'smtp.jedi.net',
    port: 587,
    secure: true,
    username: 'voldemor',
    password: 'the mot you can write',
    sslProtocols: 'TLSv1.3 TLSv1.2',
    requireTls: true,
    sslCheckServerIdentity: true,
    from: 'noreply@ght1pc9kc.fr',
    pollingIntervalSeconds: 60,
  };

  // À brancher plus tard sur ton service de persistance de config
  private onClickSaveConfig(): void {
    // TODO: appeler un service d’API pour sauvegarder la configuration
    // ex: ConfigService.updateMailConfig(this.mailConfig).subscribe(...)
    // Pour l’instant, simple trace :
    // eslint-disable-next-line no-console
    console.debug('Saving mail configuration', this.mailConfig);
  }
}
</script>