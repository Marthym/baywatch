<template>
  <curtain-modal v-slot="curtainModal" @leave="close()">
    <h2 class="font-sans text-xl border-b border-accent/40 pb-2">{{ t('security.passreset.title') }}</h2>
    <div class="m-4 max-w-lg">
      <fieldset class="fieldset w-full">
        <legend class="fieldset-legend first-letter:capitalize">{{ t('security.passreset.new') }}</legend>
        <div class="join">
          <input v-model="passwordNew" :class="{'input-error': errors.has('password')}"
                 :type="passwordVisible?'text':'password'" class="join-item input border-r-0 w-full"
                 @keyup="onFieldChange('password')"
                 @blur.stop="onBlurNewPassword"/>
          <button :class="{'input-error': errors.has('password')}"
                  class="btn btn-neutral input input-bordered border-x-0 join-item max-w-fit focus:outline-hidden"
                  @click.prevent.stop="passwordVisible = !passwordVisible">
            <EyeIcon v-if="!passwordVisible" class="h-6 w-6 opacity-50"/>
            <EyeSlashIcon v-else class="h-6 w-6 opacity-50"/>
          </button>
          <button class="btn join-item" @click.prevent.stop="onPasswordGenerate">
            {{ t('security.passreset.generate') }}
          </button>
        </div>
        <p class="label">{{ errors.get('password') }}&nbsp;</p>
      </fieldset>

      <fieldset class="fieldset">
        <legend class="fieldset-legend first-letter:capitalize">{{ t('security.passreset.confirm') }}</legend>
        <input v-model="passwordConfirm" :class="{'input-error': errors.has('passwordConfirm')}"
               class="input w-full" type="password"
               @blur="onBlurConfirmPassword"
               @change="onFieldChange('passwordConfirm')"/>
        <p class="label">{{ errors.get('confirmation') }}&nbsp;</p>
      </fieldset>

      <div class="text-right">
        <button class="btn mx-1 capitalize" @click.stop="curtainModal.close()">{{ t('dialog.cancel') }}</button>
        <button class="btn btn-primary capitalize mx-1" @click.stop="onPasswordResetClick(curtainModal)">{{
            t('security.passreset.dialog.submit')
          }}
        </button>
      </div>
    </div>
  </curtain-modal>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import CurtainModal, { CurtainModalSlot } from '@/common/components/CurtainModal.vue';
import { passwordGenerate, passwordReset } from '@/security/services/PasswordService';
import { EyeIcon, EyeSlashIcon } from '@heroicons/vue/24/solid';
import { useI18n } from 'vue-i18n';
import { Router, useRouter } from 'vue-router';
import notificationService from '@/services/notification/NotificationService';

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
export default class PasswordResetComponent extends Vue {
  private readonly router!: Router;
  private readonly t!;
  private errors: Map<string, string> = new Map<string, string>([]);

  private passwordNew: string = '';
  private passwordConfirm: string = '';
  private passwordVisible: boolean = false;
  private token: string = '';

  private mounted(): void {
    if (!this.$route.query.token) {
      this.close();
      return;
    }
    this.token = this.$route.query.token;
  }

  private onFieldChange(field: string): void {
    this.errors.delete(field);
  }

  private onPasswordGenerate(): void {
    passwordGenerate(20).subscribe({
      next: passwords => {
        this.errors.delete('password');
        let randomValue = new Uint32Array(1);
        crypto.getRandomValues(randomValue);
        this.passwordNew = passwords[randomValue[0] % 19];
      },
      error: err => this.errors.set('password', err.message),
    });
  }

  private onBlurNewPassword(): void {
    if (!this.passwordNew || this.passwordNew.length === 0) {
      return;
    } else if (!this.passwordNew || this.passwordNew.length <= 3) {
      this.errors.set('password', 'This password is not secure. An attacker will find it instant !');
      return;
    }
  }

  private onBlurConfirmPassword(): void {
    if (this.passwordConfirm && this.passwordConfirm.length > 3 && this.passwordConfirm === this.passwordNew) {
      this.errors.delete('passwordConfirm');
    } else {
      this.errors.set('passwordConfirm', 'The new and confirmation passwords must be the same');
    }
  }

  private close(): void {
    this.router.push('/');
  }

  private onPasswordResetClick(curtainModal: CurtainModalSlot): void {
    if (!this.passwordNew) {
      this.errors.set('password', 'Password is mandatory !');
    } else if (this.passwordNew !== this.passwordConfirm) {
      this.errors.set('passwordConfirm', 'Password confirmation doesn\'t match !');
    }
    if (this.errors.size !== 0) {
      return;
    }

    passwordReset(this.token, this.passwordNew).subscribe({
      next: () => {
        curtainModal.close();
        notificationService.pushSimpleOk('User account registered Successfully !');
      },
      error: err => {
        if (err.properties) {
          err.properties.forEach(p => {
            if (['password', 'passwordConfirm'].includes(p)) {
              this.errors.set(p, err.message);
            }
          });
        }
        notificationService.pushSimpleError(this.t(err.code) ?? err.message);
      },
    });
  }
}
</script>
