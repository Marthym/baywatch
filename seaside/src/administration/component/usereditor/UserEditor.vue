<template>
  <div class="grid bg-base-200 bg-opacity-60 z-30 w-full h-full absolute top-0 left-0 overflow-hidden"
       @click="opened = false">
    <Transition
        enter-active-class="lg:duration-300 ease-in-out"
        enter-from-class="lg:transform lg:translate-x-full"
        enter-to-class="lg:translate-x-0"
        leave-active-class="lg:duration-300 ease-in-out"
        leave-from-class="lg:translate-x-0"
        leave-to-class="lg:transform lg:translate-x-full"
        @after-leave="onTransitionLeave">
      <form v-if="opened"
            class="justify-self-end flex flex-col bg-base-100 text-base-content lg:w-3/4 w-full h-full overflow-auto p-2"
            @click.stop @submit.prevent="onSaveUser">
        <h2 class="font-sans text-xl border-b border-accent/40 pb-2 w-full first-letter:capitalize">{{ title }}</h2>
        <div class="flex flex-wrap content-start ">
          <div class="grow lg:basis-1/2 h-fit p-4">
            <div class="label">
              <span class="label-text capitalize">{{ t('admin.users.login') }}</span>
            </div>
            <input v-model="modelValue.login" :class="{'input-error': errors.has('login')}" :disabled="isEditionMode"
                   class="input input-bordered w-full" type="text"
                   @change="onFieldChange('login')">
            <div class="label -mt-1 mb-2">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('login')" class="label-text-alt text-error-content">{{
                  errors.get('login')
                }}</span>
            </div>

            <div class="label -mt-6">
              <span class="label-text capitalize">{{ t('admin.users.username') }}</span>
            </div>
            <input v-model="modelValue.name" :class="{'input-error': errors.has('name')}"
                   class="input input-bordered w-full"
                   type="text" @change="onFieldChange('name')">
            <div class="label -mt-1 mb-2">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('name')" class="label-text-alt text-error-content">{{ errors.get('name') }}</span>
            </div>

            <div class="label -mt-6">
              <span class="label-text capitalize">{{ t('admin.users.mail') }}</span>
            </div>
            <input v-model="modelValue.mail" :class="{'input-error': errors.has('mail')}"
                   class="input input-bordered w-full" type="email"
                   @change="onFieldChange('mail')">
            <div class="label -mt-1 mb-2">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('mail')" class="label-text-alt text-error-content">{{ errors.get('mail') }}</span>
            </div>
          </div>
          <div class="grow lg:basis-1/2 h-fit p-4">
            <div class="label">
              <span class="label-text capitalize">{{ t('admin.users.password') }}</span>
            </div>
            <div class="join w-full">
              <input v-model="modelValue.password" :class="{'input-error': errors.has('password')}"
                     :type="visible.password?'text':'password'"
                     class="input input-bordered join-item w-full"
                     @keyup="onFieldChange('password')"
                     @blur.stop="onBlurNewPassword">
              <button :class="{'input-error': errors.has('password')}"
                      class="btn btn-neutral input input-bordered border-l-0 join-item focus:outline-none"
                      @click.prevent.stop="visible.password = !visible.password">
                <EyeIcon v-if="!visible.password" class="h-6 w-6 opacity-50"/>
                <EyeSlashIcon v-else class="h-6 w-6 opacity-50"/>
              </button>
              <button class="btn join-item" @click.prevent.stop="onPasswordGenerate">
                {{ t('admin.users.editor.button.generate') }}
              </button>
            </div>
            <div class="label -mt-1 mb-2">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('password')" class="label-text-alt text-error-content">
                {{ errors.get('password') }}
              </span>
            </div>

            <div class="label -mt-6">
              <span class="label-text capitalize">{{ t('admin.users.confirmation') }}</span>
            </div>
            <div class="join w-full">
              <input v-model="passwordConfirm" :class="{'input-error': errors.has('confirm')}"
                     :type="visible.confirm?'text':'password'"
                     class="input input-bordered join-item w-full"
                     @blur="onBlurConfirmPassword"
                     @change="onFieldChange('confirm')">
              <button :class="{'input-error': errors.has('confirm')}"
                      class="btn btn-neutral input input-bordered border-l-0 join-item focus:outline-none"
                      @click.prevent.stop="visible.confirm = !visible.confirm">
                <EyeIcon v-if="!visible.confirm" class="h-6 w-6 opacity-50"/>
                <EyeSlashIcon v-else class="h-6 w-6 opacity-50"/>
              </button>
            </div>
            <div class="label -mt-1 mb-2">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('confirm')" class="label-text-alt text-error-content">
                {{ errors.get('confirm') }}
              </span>
            </div>
          </div>
          <div :class="{'border': errors.has('roles')}"
               class="grow lg:basis-1/2 h-fit p-4 border-error rounded-lg">
            <UserRoleInput :model-value="modelValue.roles" @update:modelValue="onRoleUpdate"/>
            <span v-if="errors.has('roles')" class="label-text-alt text-error-content">{{ errors.get('roles') }}</span>
          </div>
        </div>
        <span class="grow"></span>
        <div>
          <button class="btn m-2" @click.prevent.stop="onCancel">{{ t('admin.users.editor.button.cancel') }}</button>
          <button :disabled="!hasValidRoles" class="btn btn-primary m-2" @click.prevent.stop="onSaveUser">
            {{ t('admin.users.editor.button.save') }}
          </button>
        </div>
      </form>
    </Transition>

  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import { User } from '@/security/model/User';
import UserRoleInput from '@/administration/component/usereditor/UserRoleInput.vue';
import { MAIL_PATTERN, ULID_PATTERN } from '@/common/services/RegexPattern';
import { EyeIcon } from '@heroicons/vue/24/outline';
import { useI18n } from 'vue-i18n';
import { EyeSlashIcon } from '@heroicons/vue/24/solid';
import { passwordAnonymousCheckStrength, passwordGenerate } from '@/security/services/PasswordService';

const CANCEL_EVENT: string = 'cancel';
const SUBMIT_EVENT: string = 'submit';
const CHANGE_EVENT: string = 'change';
const FIELD_PASSWORD: string = 'password';
const FIELD_CONFIRM: string = 'confirm';

@Component({
  name: 'UserEditor',
  components: { EyeSlashIcon, EyeIcon, UserRoleInput },
  emits: [CANCEL_EVENT, SUBMIT_EVENT, CHANGE_EVENT],
  setup() {
    const { t } = useI18n();
    return { t };
  },
})
export default class UserEditor extends Vue {
  @Prop() private modelValue: User;
  private readonly t;
  private passwordConfirm: string = '';
  private title: string = '';
  private isEditionMode: boolean = false;
  private errors: Map<string, string> = new Map<string, string>();
  private visible = {
    password: false,
    confirm: false,
  };
  private opened: boolean = false;
  private closeEvent: typeof SUBMIT_EVENT | typeof CANCEL_EVENT = CANCEL_EVENT;

  get hasValidRoles(): boolean {
    return this.modelValue.roles.findIndex(r => !ULID_PATTERN.test(r)) == -1;
  }

  mounted(): void {
    this.isEditionMode = '_id' in this.modelValue && this.modelValue._id !== undefined;
    this.title = this.isEditionMode
        ? this.t('admin.users.editor.title.update', { login: this.modelValue.login })
        : this.t('admin.users.editor.title.create');
    this.$nextTick(() => this.opened = true);
  }

  private onFieldChange(field: string): void {
    this.errors.delete(field);
  }

  private onPasswordGenerate(): void {
    passwordGenerate(20).subscribe({
      next: passwords => {
        this.errors.delete(FIELD_PASSWORD);
        let randomValue = new Uint32Array(1);
        crypto.getRandomValues(randomValue);
        this.modelValue.password = passwords[randomValue[0] % 19];
        this.passwordConfirm = this.modelValue.password;
      },
      error: err => this.errors.set(FIELD_PASSWORD, err.message),
    });
  }

  private onBlurNewPassword(): void {
    if (!this.modelValue.password || this.modelValue.password.length === 0) {
      return;
    } else if (!this.modelValue.password || this.modelValue.password.length <= 3) {
      this.errors.set(FIELD_PASSWORD, this.t('admin.users.editor.message.password_too_short'));
      return;
    }
    if (!this.modelValue.login) {
      this.errors.set(FIELD_PASSWORD, this.t('admin.users.editor.message.login_field_required'));
      return;
    }
    passwordAnonymousCheckStrength(this.modelValue).subscribe({
      next: evaluation => {
        if (evaluation.isSecure) {
          this.errors.delete(FIELD_PASSWORD);
        } else {
          this.errors.set(FIELD_PASSWORD, evaluation.message);
        }
      },
      error: err => this.errors.set(FIELD_PASSWORD, err.message),
    });
  }

  private onBlurConfirmPassword(): void {
    console.log(this.passwordConfirm === this.modelValue.password);
    if (this.passwordConfirm && this.passwordConfirm.length > 3 && this.passwordConfirm === this.modelValue.password) {
      this.errors.delete(FIELD_CONFIRM);
    } else {
      this.errors.set(FIELD_CONFIRM, this.t('admin.users.editor.message.wrong_confirmation'));
    }
  }

  private onCancel(): void {
    this.closeEvent = CANCEL_EVENT;
    this.opened = false;
  }

  private onSaveUser(): void {
    console.debug('onSaveUser');
    if (!this.modelValue.login) {
      this.errors.set('login', this.t('admin.users.editor.message.login_mandatory'));
    }
    if (!this.modelValue.name) {
      this.errors.set('name', this.t('admin.users.editor.message.name_mandatory'));
    }
    if (!this.modelValue.mail) {
      this.errors.set('mail', this.t('admin.users.editor.message.mail_mandatory'));
    } else if (!MAIL_PATTERN.test(this.modelValue.mail)) {
      this.errors.set('mail', this.t('admin.users.editor.message.mail_incorrect'));
    }
    if (!this.modelValue.roles || this.modelValue.roles.length === 0) {
      this.errors.set('roles', this.t('admin.users.editor.message.role_mandatory'));
    }
    if (!this.hasValidRoles) {
      this.errors.set('roles', this.t('admin.users.editor.message.role_incorrect', { pattern: ULID_PATTERN }));
    }
    if (!this.isEditionMode) {
      if (!this.modelValue.password) {
        this.errors.set(FIELD_PASSWORD, this.t('admin.users.editor.message.password_mandatory'));
      } else if (this.modelValue.password !== this.passwordConfirm) {
        this.errors.set(FIELD_CONFIRM, this.t('admin.users.editor.message.wrong_confirmation'));
      }
    }

    if (this.errors.size === 0) {
      this.closeEvent = SUBMIT_EVENT;
      this.opened = false;
    }
  }

  private onRoleUpdate(event: Event): void {
    this.errors.delete('roles');
    this.modelValue.roles.splice(0, this.modelValue.roles.length, ...event);
  }

  private onTransitionLeave(): void {
    if (this.closeEvent === CANCEL_EVENT) {
      this.$emit(this.closeEvent);
    } else {
      this.$emit(this.closeEvent, this.modelValue);
    }
  }
}
</script>