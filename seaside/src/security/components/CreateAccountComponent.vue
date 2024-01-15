<template>
  <curtain-modal @leave="close()" v-slot="curtainModal">
    <h2 class="font-sans text-xl border-b border-accent/40 pb-2">Create new account</h2>
    <div class="m-4 max-w-lg">
      <label class="label">
        <span class="label-text">Login</span>
      </label>
      <input v-model="account.login" type="text" placeholder="login"
             class="input input-bordered w-full"
             :class="{'input-error': errors.has('login')}" @change="onFieldChange('login')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('login')" class="label-text-alt">{{ errors.get('login') }}</span>
      </label>

      <label class="label -mt-6">
        <span class="label-text">Username</span>
      </label>
      <input v-model="account.name" type="text" placeholder="username"
             class="input input-bordered w-full"
             :class="{'input-error': errors.has('name')}" @change="onFieldChange('name')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('name')" class="label-text-alt">{{ errors.get('name') }}</span>
      </label>

      <label class="label -mt-6">
        <span class="label-text">Mail</span>
      </label>
      <input v-model="account.mail" type="email" placeholder="mail address" name="ukyilkil"
             class="input input-bordered w-full"
             :class="{'input-error': errors.has('mail')}" @change="onFieldChange('mail')">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('mail')" class="label-text-alt">{{ errors.get('mail') }}</span>
      </label>

      <label class="label">
        <span class="label-text">Password</span>
      </label>
      <div class="tooltip tooltip-error tooltip-bottom w-full" :data-tip="errors.get('password')"
           :class="{'tooltip-open': errors.has('password')}">
        <div class="join w-full">
          <input v-model="account.password" :type="passwordVisible?'text':'password'"
                 class="input input-bordered join-item w-full"
                 :class="{'input-error': errors.has('password')}"
                 @keyup="onFieldChange('password')"
                 @blur="onBlurNewPassword">
          <button class="btn btn-neutral input input-bordered border-l-0 join-item focus:outline-none"
                  :class="{'input-error': errors.has('password')}"
                  @click="passwordVisible = !passwordVisible">
            <EyeIcon class="h-6 w-6 opacity-50"/>
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
        <button class="btn btn-sm btn-primary mx-1" @click.stop="onRegisterClick">Register</button>
      </div>
    </div>
  </curtain-modal>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
import CurtainModal from '@/common/components/CurtainModal.vue';
import { User } from '@/security/model/User';
import { Store, useStore } from 'vuex';
import { UserState } from '@/security/store/user';
import { CLOSE_CREATE_ACCOUNT_MUTATION } from '@/security/store/UserConstants';
import { passwordAnonymousCheckStrength, passwordGenerate } from '@/security/services/PasswordService';
import { EyeIcon } from '@heroicons/vue/24/solid';
import { userCreate } from '@/security/services/UserService';
import notificationService from '@/services/notification/NotificationService';
import { useKeyboardController } from '@/common/services/KeyboardController';

const CLOSE_EVENT: string = 'close';

@Component({
  emits: [CLOSE_EVENT],
  components: { CurtainModal, EyeIcon },
  setup() {
    return {
      userStore: useStore(),
      keyboardController: useKeyboardController(),
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
      next: passwords => this.account.password = passwords[Math.floor(Math.random() * 19)],
      error: err => this.errors.set('password', err.message),
    });
  }

  private onBlurNewPassword(): void {
    if (!this.account.password || this.account.password.length <= 3) {
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

  private onRegisterClick(): void {
    userCreate(this.account).subscribe({
      next: () => notificationService.pushSimpleOk('User account registered Successfully !'),
      error: err => notificationService.pushSimpleError(err.message),
    });
  }
}
</script>
