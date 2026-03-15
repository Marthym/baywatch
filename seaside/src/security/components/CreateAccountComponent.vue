<template>
  <curtain-modal v-slot="curtainModal" @leave="close()">
    <h2 class="font-sans text-xl border-b border-accent/40 pb-2">{{ t('security.register.title') }}</h2>
    <div class="m-4 max-w-lg [&_input]:text-base-content [&_label]:first-letter:uppercase">
      <fieldset class="fieldset bg-base-200 border-base-300 rounded-box w-full border p-4">

        <label class="label block">{{ t('security.register.login') }}
          <input v-model="account.login" :class="{'input-error': errors.has('login')}" class="input block w-full mt-1"
                 type="text"
                 @change="errors.delete('login')"/>
          <span :class="{'text-error': errors.has('login')}" class="label">{{ errors.get('login') }}&nbsp;</span>
        </label>

        <label class="label block">{{ t('security.register.username') }}
          <input v-model="account.name" :class="{'input-error': errors.has('name')}" class="input w-full block mt-1"
                 type="text"
                 @change="errors.delete('name')"/>
          <span :class="{'text-error': errors.has('name')}" class="label">{{ errors.get('name') }}&nbsp;</span>
        </label>

        <label class="label block">{{ t('security.register.mail') }}
          <input v-model="account.mail" :class="{'input-error': errors.has('mail')}" class="input w-full block mt-1"
                 type="email"
                 @change="errors.delete('mail')"/>
          <span :class="{'text-error': errors.has('mail')}" class="label">{{ errors.get('mail') }}&nbsp;</span>
        </label>
      </fieldset>

      <div class="card-actions justify-end pt-2">
        <button class="btn mx-1 capitalize" @click.stop="curtainModal.close()">{{
            t('dialog.cancel') || 'cancel'
          }}
        </button>
        <button class="btn btn-primary capitalize mx-1" @click.stop="onRegisterClick(curtainModal)">{{
            t('security.register.dialog.register') || 'register'
          }}
        </button>
      </div>

    </div>
  </curtain-modal>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import CurtainModal, { CurtainModalSlot } from '@/common/components/CurtainModal.vue';
import { UserAccountForm, UserAccountFormSchema, UserCreated } from '@/security/model/User';
import { EyeIcon, EyeSlashIcon } from '@heroicons/vue/24/solid';
import { userCreate } from '@/security/services/UserService';
import notificationService from '@/services/notification/NotificationService';
import { useI18n } from 'vue-i18n';
import { Router, useRouter } from 'vue-router';
import { TranslatorFunction } from '@/i18n';
import { isValiError, parse, ValiError } from 'valibot';

const CLOSE_EVENT: string = 'close';

@Component({
  emits: [CLOSE_EVENT],
  components: { CurtainModal, EyeIcon, EyeSlashIcon },
  setup() {
    const { t } = useI18n();
    const router = useRouter();
    return { t, router };
  },
})
export default class CreateAccountComponent extends Vue {
  private readonly router!: Router;
  private readonly t!: TranslatorFunction;
  private errors: Map<string, string> = new Map<string, string>([]);

  private account: UserAccountForm = {
    login: '',
    name: '',
    mail: '',
  };

  private close(): void {
    this.router.push('/');
  }

  private onRegisterClick(curtainModal: CurtainModalSlot): void {
    try {
      parse(UserAccountFormSchema, this.account);

      const user: UserCreated = {
        login: this.account.login,
        name: this.account.name,
        mail: this.account.mail,
        password: undefined,
        roles: [],
      };

      userCreate(user).subscribe({
        next: () => {
          curtainModal.close();
          notificationService.pushSimpleOk(this.t('security.register.message.save.successfully'));
        },
        error: err => {
          if (err.properties) {
            err.properties
                .filter((p: string) => Object.keys(this.account).includes(p))
                .forEach((p: string) => this.errors.set(p, err.message));
          }
          notificationService.pushSimpleError(err.message);
        },
      });

    } catch (e) {
      if (isValiError(e)) {
        this.handleValiError(e as ValiError<typeof UserAccountFormSchema>);
      }
      notificationService.pushSimpleError(this.t('security.register.message.formValidationError'));
      console.debug(this.errors);
    }
  }

  private handleValiError(error: ValiError<typeof UserAccountFormSchema>): void {
    this.errors.clear();
    error.issues.forEach((value) => {
      if (value.path) {
        this.errors.set(value.path.map((p: { key: any }) => p.key).join('.'), this.t(value.message));
      } else {
        console.debug('No path for validation error: ', value);
      }
    });
  }
}
</script>
