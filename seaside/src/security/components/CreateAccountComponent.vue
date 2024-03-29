<template>
  <curtain-modal @leave="close()" v-slot="curtainModal">
    <h2 class="font-sans text-xl border-b border-accent/40 pb-2">Create new account</h2>
    <div class="m-4 max-w-lg">
      <label class="label">
        <span class="label-text">Login</span>
      </label>
      <input v-model="account.login" type="text" placeholder="login"
             class="input input-bordered w-full"
             :class="{'input-error': errors.has('login')}"
             @change="onFieldChange('login')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('login')" class="label-text-alt">{{ errors.get('login') }}</span>
      </label>

      <label class="label -mt-6">
        <span class="label-text">Username</span>
      </label>
      <input v-model="account.name" type="text" placeholder="User Name"
             class="input input-bordered w-full"
             :class="{'input-error': errors.has('name')}" @change="onFieldChange('name')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('name')" class="label-text-alt">{{ errors.get('name') }}</span>
      </label>

      <label class="label -mt-6">
        <span class="label-text">Mail</span>
      </label>
      <input v-model="account.mail" type="email" placeholder="okenobi@ght1pc9kc.fr"
             class="input input-bordered w-full"
             :class="{'input-error': errors.has('mail')}" @change="onFieldChange('mail')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('mail')" class="label-text-alt">{{ errors.get('mail') }}</span>
      </label>

      <label class="label">
        <span class="label-text">Password</span>
      </label>
      <div class="tooltip-error tooltip-bottom w-full" :data-tip="errors.get('password')"
           :class="{'tooltip tooltip-open': errors.has('password')}">
        <div class="join w-full">
          <input v-model="account.password" :type="passwordVisible?'text':'password'"
                 class="input input-bordered join-item w-full"
                 :class="{'input-error': errors.has('password')}"
                 @keyup="onFieldChange('password')"
                 @blur.stop="onBlurNewPassword">
          <button class="btn btn-neutral input input-bordered border-l-0 join-item focus:outline-none"
                  :class="{'input-error': errors.has('password')}"
                  @click="passwordVisible = !passwordVisible">
            <EyeIcon v-if="!passwordVisible" class="h-6 w-6 opacity-50"/>
            <EyeSlashIcon v-else class="h-6 w-6 opacity-50"/>
          </button>
          <button class="btn join-item" @click.stop="onPasswordGenerate">Generate</button>
        </div>
      </div>

      <label class="label">
        <span class="label-text">Password Confirmation</span>
      </label>
      <input v-model="passwordConfirm" type="password" class="input input-bordered w-full"
             :class="{'input-error': errors.has('passwordConfirm')}"
             @change="onFieldChange('passwordConfirm')"
             @blur="onBlurConfirmPassword">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('passwordConfirm')" class="label-text-alt">{{ errors.get('passwordConfirm') }}</span>
      </label>

      <div class="text-right">
        <button class="btn btn-sm mx-1" @click.stop="curtainModal.close()">Cancel</button>
        <button class="btn btn-sm btn-primary mx-1" @click.stop="onRegisterClick(curtainModal)">Register</button>
      </div>
    </div>
  </curtain-modal>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import CurtainModal, { CurtainModalSlot } from '@/common/components/CurtainModal.vue';
import { User } from '@/security/model/User';
import { Store, useStore } from 'vuex';
import { UserState } from '@/security/store/user';
import { CLOSE_CREATE_ACCOUNT_MUTATION } from '@/security/store/UserConstants';
import { passwordAnonymousCheckStrength, passwordGenerate } from '@/security/services/PasswordService';
import { EyeIcon, EyeSlashIcon } from '@heroicons/vue/24/solid';
import { userCreate } from '@/security/services/UserService';
import notificationService from '@/services/notification/NotificationService';
import { MAIL_PATTERN } from '@/common/services/RegexPattern';

const CLOSE_EVENT: string = 'close';

@Component({
  emits: [CLOSE_EVENT],
  components: { CurtainModal, EyeIcon, EyeSlashIcon },
  setup() {
    return {
      userStore: useStore(),
    };
  },
})
export default class CreateAccountComponent extends Vue {
  private readonly userStore: Store<UserState>;
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
    this.userStore.commit(CLOSE_CREATE_ACCOUNT_MUTATION);
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
        console.debug(err);
        console.debug(err.properties);
        if (err.properties) {
          err.properties.forEach(p => {
            console.debug(p);
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
