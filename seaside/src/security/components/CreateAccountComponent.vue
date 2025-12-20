<template>
  <curtain-modal v-slot="curtainModal" @leave="close()">
    <h2 class="font-sans text-xl border-b border-accent/40 pb-2">{{ t('security.register.title') }}</h2>
    <div class="m-4 max-w-lg [&_input]:text-base-content [&_label]:first-letter:uppercase">
      <fieldset class="fieldset bg-base-200 border-base-300 rounded-box w-full border p-4">

        <label class="label block">{{ t('security.register.login') }}
          <input v-model="account.login" :class="{'input-error': errors.has('login')}" class="input block w-full mt-1" type="text"
                 @change="errors.delete('login')"/>
          <span class="label" :class="{'text-error': errors.has('login')}">{{ errors.get('login') }}&nbsp;</span>
        </label>

        <label class="label block">{{ t('security.register.username') }}
          <input v-model="account.name" :class="{'input-error': errors.has('name')}" class="input w-full block mt-1" type="text"
                 @change="errors.delete('name')"/>
          <span class="label" :class="{'text-error': errors.has('name')}">{{ errors.get('name') }}&nbsp;</span>
        </label>

        <label class="label block">{{ t('security.register.mail') }}
          <input v-model="account.mail" :class="{'input-error': errors.has('mail')}" class="input w-full block mt-1" type="email"
                 @change="errors.delete('mail')"/>
          <span class="label" :class="{'text-error': errors.has('mail')}">{{ errors.get('mail') }}&nbsp;</span>
        </label>

        <span class="label block first-letter:uppercase">{{ t('security.register.password') }}</span>
        <div class="join">
          <input v-model="account.password" :class="{'input-error': errors.has('password')}"
                 :type="passwordVisible?'text':'password'" class="join-item input border-r-0 w-full"
                 @keyup="onFieldChange('password')"
                 @blur.stop="onBlurNewPassword"/>
          <button :class="{'input-error': errors.has('password')}"
                  class="btn btn-neutral input input-bordered border-x-0 join-item max-w-fit focus:outline-hidden"
                  @click.prevent.stop="passwordVisible = !passwordVisible">
            <EyeIcon v-if="!passwordVisible" class="h-6 w-6 opacity-50"/>
            <EyeSlashIcon v-else class="h-6 w-6 opacity-50"/>
          </button>
          <button class="btn btn-soft join-item"
                  :class="{'border-error border': errors.has('password')}"
                  @click.prevent.stop="onPasswordGenerate">
            {{ t('security.register.generate') || 'generate' }}
          </button>
        </div>
        <p class="label" :class="{'text-error': errors.has('password')}">{{ errors.get('password') }}&nbsp;</p>

      <label class="label block">{{ t('security.register.confirmation') }}
        <input v-model="passwordConfirm" :class="{'input-error': errors.has('passwordConfirm')}"
               class="input w-full block mt-1" type="password"
               @blur="onBlurConfirmPassword"
               @change="onFieldChange('passwordConfirm')"/>
        <span class="label" :class="{'text-error': errors.has('confirmation')}">{{ errors.get('confirmation') }}&nbsp;</span>
      </label>
      </fieldset>

      <div class="card-actions justify-end pt-2">
        <button class="btn mx-1 capitalize" @click.stop="curtainModal.close()">{{ t('dialog.cancel') || 'cancel' }}</button>
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
import { User } from '@/security/model/User';
import { passwordAnonymousCheckStrength, passwordGenerate } from '@/security/services/PasswordService';
import { EyeIcon, EyeSlashIcon } from '@heroicons/vue/24/solid';
import { userCreate } from '@/security/services/UserService';
import notificationService from '@/services/notification/NotificationService';
import { MAIL_PATTERN } from '@/common/services/RegexPattern';
import { useI18n } from 'vue-i18n';
import { Router, useRouter } from 'vue-router';

const CLOSE_EVENT: string = 'close';

@Component({
  emits: [CLOSE_EVENT],
  components: { CurtainModal, EyeIcon, EyeSlashIcon },
  setup() {
    const { t } = useI18n();
    const router = useRouter();
    return {
      t: t,
      router: router,
    };
  },
})
export default class CreateAccountComponent extends Vue {
  private readonly router!: Router;
  private readonly t!;
  private errors: Map<string, string> = new Map<string, string>([]);

  private account: User = { roles: [] } as User;
  private passwordConfirm: string = '';
  private passwordVisible: boolean = false;

  private onFieldChange(field: string): void {
    this.errors.delete(field);
  }

  private onPasswordGenerate(): void {
    passwordGenerate(20).subscribe({
      next: passwords => {
        this.errors.delete('password');
        let randomValue = new Uint32Array(1);
        crypto.getRandomValues(randomValue);
        this.account.password = passwords[randomValue[0] % 19];
      },
      error: err => this.errors.set('password', err.message),
    });
  }

  private onBlurNewPassword(): void {
    if (!this.account.password || this.account.password.length === 0) {
      return;
    } else if (!this.account.password || this.account.password.length <= 3) {
      this.errors.set('password', 'This password is not secure. An attacker will find it instant !');
      return;
    }
    if (!this.account.login) {
      this.errors.set('password', 'Login field is required to check password strength');
      return;
    }
    passwordAnonymousCheckStrength(this.account).subscribe({
      next: evaluation => {
        if (evaluation.isSecure) {
          this.errors.delete('password');
        } else {
          this.errors.set('password', evaluation.message);
        }
      },
      error: err => this.errors.set('password', err.message),
    });
  }

  private onBlurConfirmPassword(): void {
    if (this.passwordConfirm && this.passwordConfirm.length > 3 && this.passwordConfirm === this.account.password) {
      this.errors.delete('passwordConfirm');
    } else {
      this.errors.set('passwordConfirm', 'The new and confirmation passwords must be the same');
    }
  }

  private close(): void {
    this.router.push('/');
  }

  private onRegisterClick(curtainModal: CurtainModalSlot): void {
    if (!this.account.login || this.account.login.length < 3) {
      this.errors.set('login', 'Invalid login !');
    }
    if (!this.account.mail || this.account.mail.length < 3) {
      this.errors.set('mail', 'Invalid mail address !');
    } else if (!MAIL_PATTERN.test(this.account.mail)) {
      this.errors.set('mail', 'Mail address must be syntactically correct !');
    }
    if (!this.account.password) {
      this.errors.set('password', 'Password is mandatory !');
    } else if (this.account.password !== this.passwordConfirm) {
      this.errors.set('passwordConfirm', 'Password confirmation doesn\'t match !');
    }
    if (this.errors.size !== 0) {
      return;
    }

    userCreate(this.account).subscribe({
      next: () => {
        curtainModal.close();
        notificationService.pushSimpleOk('User account registered Successfully !');
      },
      error: err => {
        if (err.properties) {
          err.properties.forEach(p => {
            if (['mail', 'password', 'login', 'passwordConfirm'].includes(p)) {
              this.errors.set(p, err.message);
            }
          });
        }
        notificationService.pushSimpleError(err.message);
      },
    });
  }
}
</script>
