<template>
  <div class="grid bg-base-200/60 z-30 w-full h-full absolute top-0 left-0 overflow-hidden"
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
            class="justify-self-end flex flex-col bg-base-100 text-base-content lg:w-3/4 w-full h-full overflow-auto p-6"
            @click.stop @submit.prevent="onSaveUser">
        <h2 class="card-title text-2xl pb-2 w-full first-letter:capitalize">{{ title }}</h2>
        <p class="text-sm text-base-content/70 mb-4">
          {{ isEditionMode ? t('admin.users.editor.subtitle.update') : t('admin.users.editor.subtitle.create') }}
        </p>

        <div class="space-y-6 [&_input]:text-base-content [&_label]:first-letter:uppercase">
          <!-- Informations générales -->
          <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
            <legend class="fieldset-legend">{{ t('admin.users.section.general') || 'General Information' }}</legend>

            <label class="label block">
              {{ t('admin.users.login') }}
              <input v-model="modelValue.login"
                     :class="{'input-error': errors.has('login')}"
                     :disabled="isEditionMode"
                     class="input input-bordered w-full block"
                     type="text"
                     @input="onFieldChange('login')">
              <span v-if="errors.has('login')" class="label text-error text-xs">{{ errors.get('login') }}</span>
            </label>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <label class="label block">
                {{ t('admin.users.username') }}
                <input v-model="modelValue.name"
                       :class="{'input-error': errors.has('name')}"
                       class="input input-bordered w-full block"
                       type="text"
                       @input="onFieldChange('name')">
                <span v-if="errors.has('name')" class="label text-error text-xs">{{ errors.get('name') }}</span>
              </label>

              <label class="label block">
                {{ t('admin.users.mail') }}
                <input v-model="modelValue.mail"
                       :class="{'input-error': errors.has('mail')}"
                       class="input input-bordered w-full block"
                       type="email"
                       @input="onFieldChange('mail')">
                <span v-if="errors.has('mail')" class="label text-error text-xs">{{ errors.get('mail') }}</span>
              </label>
            </div>
          </fieldset>

          <!-- Sécurité / Mot de passe -->
          <fieldset class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
            <legend class="fieldset-legend">{{ t('admin.users.section.security') || 'Security' }}</legend>

            <span class="label block first-letter:uppercase">{{ t('admin.users.password') }}</span>
            <div class="join w-full">
              <input v-model="modelValue.password"
                     :class="{'input-error': errors.has('password')}"
                     :type="visible.password?'text':'password'"
                     class="input input-bordered join-item w-full"
                     @input="onFieldChange('password')"
                     @blur.stop="onBlurNewPassword">
              <button class="btn btn-neutral input input-bordered border-x-0 join-item max-w-fit focus:outline-hidden"
                      type="button"
                      @click.prevent.stop="visible.password = !visible.password">
                <EyeIcon v-if="!visible.password" class="h-5 w-5 opacity-50"/>
                <EyeSlashIcon v-else class="h-5 w-5 opacity-50"/>
              </button>
              <button class="btn btn-soft join-item" type="button" @click.prevent.stop="onPasswordGenerate">
                {{ t('admin.users.editor.button.generate') }}
              </button>
            </div>
            <p v-if="errors.has('password')" class="label text-error text-xs">{{ errors.get('password') }}</p>

            <label class="label block mt-2">
              {{ t('admin.users.confirmation') }}
              <input v-model="passwordConfirm"
                     :class="{'input-error': errors.has('confirm')}"
                     :type="visible.password?'text':'password'"
                     class="input input-bordered w-full block"
                     @blur="onBlurConfirmPassword"
                     @input="onFieldChange('confirm')">

              <span v-if="errors.has('confirm')" class="label text-error text-xs">{{ errors.get('confirm') }}</span>
            </label>
          </fieldset>

          <!-- Rôles -->
          <fieldset :class="{'border-error': errors.has('roles')}"
                    class="fieldset bg-base-200 border-base-300 rounded-box border p-4">
            <legend class="fieldset-legend">{{ t('admin.users.roles.title') || 'Roles & Permissions' }}</legend>
            <UserRoleInput :model-value="modelValue.roles" @update:modelValue="onRoleUpdate"/>
            <span v-if="errors.has('roles')" class="label text-error text-xs">{{ errors.get('roles') }}</span>
          </fieldset>
        </div>

        <div class="grow"></div>

        <div class="card-actions justify-end pt-6">
          <button class="btn first-letter:uppercase" @click.prevent.stop="onCancel">
            {{ t('admin.users.editor.button.cancel') }}
          </button>
          <button :disabled="!hasValidRoles" class="btn btn-primary first-letter:uppercase"
                  @click.prevent.stop="onSaveUser">
            {{ t('admin.users.editor.button.save') }}
          </button>
        </div>
      </form>
    </Transition>

  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import { UserAccountFormSchema, UserCreated } from '@/security/model/User';
import UserRoleInput from '@/administration/component/usereditor/UserRoleInput.vue';
import { ULID_PATTERN } from '@/common/services/RegexPattern';
import { EyeIcon } from '@heroicons/vue/24/outline';
import { useI18n } from 'vue-i18n';
import { EyeSlashIcon } from '@heroicons/vue/24/solid';
import { passwordAnonymousCheckStrength, passwordGenerate } from '@/security/services/PasswordService';
import { TranslatorFunction } from '@/i18n';
import { parse, pick, ValiError } from 'valibot';

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
  @Prop() private modelValue!: UserCreated;
  private readonly t!: TranslatorFunction;
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
    return this.modelValue !== undefined && !this.modelValue.roles.some(r => !ULID_PATTERN.test(r));
  }

  mounted(): void {
    console.debug('UserEditor mounted, modelValue:', this.modelValue);
    this.isEditionMode = this.modelValue !== undefined && '_id' in this.modelValue && this.modelValue._id !== '0';
    console.debug('UserEditor mounted, isEditionMode:', this.isEditionMode);
    this.title = this.isEditionMode
        ? this.t('admin.users.editor.title.update', { login: this.modelValue?.login || 'unknown' })
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
    try {
      parse(pick(UserAccountFormSchema.pipe[0], ['login', 'password']), {
        password: this.modelValue.password,
        login: this.modelValue.login,
      });

      const user: UserCreated = {
        login: this.modelValue.login,
        name: this.modelValue.name,
        mail: this.modelValue.mail,
        password: this.modelValue.password,
        roles: [],
      };
      passwordAnonymousCheckStrength(user).subscribe({
        next: evaluation => {
          if (evaluation.isSecure) {
            this.errors.delete(FIELD_PASSWORD);
          } else {
            this.errors.set(FIELD_PASSWORD, evaluation.message);
          }
        },
        error: err => this.errors.set(FIELD_PASSWORD, err.message),
      });
    } catch (e) {
      this.handleValiError(e as ValiError<typeof UserAccountFormSchema>);
    }
  }

  private onBlurConfirmPassword(): void {
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
    try {
      parse(UserAccountFormSchema, this.modelValue);
      if (!this.hasValidRoles) {
        this.errors.set('roles', this.t('admin.users.editor.message.role_incorrect', { pattern: ULID_PATTERN }));
      }

      this.closeEvent = SUBMIT_EVENT;
      this.opened = false;
    } catch (e) {
      this.handleValiError(e as ValiError<typeof UserAccountFormSchema>);
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