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

        <span class="label block first-letter:uppercase">{{ t('security.register.password') }}</span>
        <div class="join">
          <input v-model="account.password" :class="{'input-error': errors.has('password')}"
                 :type="passwordVisible?'text':'password'" class="join-item input border-r-0 w-full"
                 @keyup="onFieldChange('password')"
                 @blur.stop="onBlurNewPassword"/>
          <button :class="{'input-error': errors.has('password')}"
                  class="btn btn-neutral input input-bordered border-x-0 join-item max-w-fit focus:outline-hidden"
                  @click.prevent.stop="passwordVisible = !passwordVisible">
            <EyeIcon v-if="!passwordVisible" class="h-6 w-6 opacity-50" alt="Show password"/>
            <EyeSlashIcon v-else class="h-6 w-6 opacity-50" alt="Hide password"/>
          </button>
          <button :class="{'border-error border': errors.has('password')}"
                  class="btn btn-soft join-item"
                  @click.prevent.stop="onPasswordGenerate">
            {{ t('security.register.generate') || 'generate' }}
          </button>
        </div>
        <p :class="{'text-error': errors.has('password')}" class="label">{{ errors.get('password') }}&nbsp;</p>

        <label class="label block">{{ t('security.register.confirmation') }}
          <input v-model="account.passwordConfirm" :class="{'input-error': errors.has('passwordConfirm')}"
                 :type="passwordVisible?'text':'password'"
                 class="input w-full block mt-1"
                 @blur="onBlurConfirmPassword"
                 @change="onFieldChange('passwordConfirm')"/>
          <span :class="{'text-error': errors.has('passwordConfirm')}" class="label">{{
              errors.get('passwordConfirm')
            }}&nbsp;</span>
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
import { passwordAnonymousCheckStrength, passwordGenerate } from '@/security/services/PasswordService';
import { EyeIcon, EyeSlashIcon } from '@heroicons/vue/24/solid';
import { userCreate } from '@/security/services/UserService';
import notificationService from '@/services/notification/NotificationService';
import { useI18n } from 'vue-i18n';
import { Router, useRouter } from 'vue-router';
import { TranslatorFunction } from '@/i18n';
import { isValiError, parse, pick, ValiError } from 'valibot';

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
    password: '',
    passwordConfirm: '',
  };
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
    try {
      parse(pick(UserAccountFormSchema.pipe[0], ['login', 'password']), {
        password: this.account.password,
        login: this.account.login,
      });

      const user: UserCreated = {
        login: this.account.login,
        name: this.account.name,
        mail: this.account.mail,
        password: this.account.password,
        roles: [],
      };

      passwordAnonymousCheckStrength(user).subscribe({
        next: evaluation => {
          if (evaluation.isSecure) {
            this.errors.delete('password');
          } else {
            this.errors.set('password', evaluation.message);
          }
        },
        error: err => this.errors.set('password', err.message),
      });

    } catch (e) {
      this.handleValiError(e as ValiError<typeof UserAccountFormSchema>);
    }
  }

  private onBlurConfirmPassword(): void {
    if (this.account.passwordConfirm
        && this.account.passwordConfirm.length > 3
        && this.account.passwordConfirm === this.account.password) {
      this.errors.delete('passwordConfirm');
    } else {
      this.errors.set('passwordConfirm', this.t('security.register.message.confirm.different.password'));
    }
  }

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
        password: this.account.password,
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
      console.error(this.errors);
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
