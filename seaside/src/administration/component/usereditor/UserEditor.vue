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
        <h2 class="font-sans text-xl border-b border-accent/40 pb-2 w-full">{{ title }}</h2>
        <div class="flex flex-wrap content-start ">
          <div class="grow lg:basis-1/2 h-fit p-4">
            <div class="label">
              <span class="label-text">Login</span>
            </div>
            <input v-model="modelValue.login" type="text" placeholder="login"
                   class="input input-bordered w-full"
                   :class="{'input-error': errors.has('login')}" @change="onFieldChange('login')"
                   :disabled="isEditionMode">
            <div class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('login')" class="label-text-alt">{{ errors.get('login') }}</span>
            </div>

            <div class="label -mt-6">
              <span class="label-text">Username</span>
            </div>
            <input v-model="modelValue.name" type="text" placeholder="username"
                   class="input input-bordered w-full"
                   :class="{'input-error': errors.has('name')}" @change="onFieldChange('name')">
            <div class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('name')" class="label-text-alt">{{ errors.get('name') }}</span>
            </div>

            <div class="label -mt-6">
              <span class="label-text">Mail</span>
            </div>
            <input v-model="modelValue.mail" type="email" placeholder="mail address"
                   class="input input-bordered w-full"
                   :class="{'input-error': errors.has('mail')}" @change="onFieldChange('mail')"
                   :disabled="isEditionMode">
            <div class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('mail')" class="label-text-alt">{{ errors.get('mail') }}</span>
            </div>
          </div>
          <div class="grow lg:basis-1/2 h-fit p-4">
            <div class="label">
              <span class="label-text">Password</span>
            </div>
            <input v-model="modelValue.password" type="password" class="input input-bordered w-full"
                   :class="{'input-error': errors.has('password')}" @change="onFieldChange('password')">
            <div class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('password')" class="label-text-alt">{{
                  errors.get('password')
                }}</span>
            </div>

            <div class="label -mt-6">
              <span class="label-text">Password Confirmation</span>
            </div>
            <input v-model="passwordConfirm" type="password" class="input input-bordered w-full"
                   :class="{'input-error': errors.has('confirm')}" @change="onFieldChange('confirm')">
            <div class="label -mt-1">
              <span class="label-text-alt">&nbsp;</span>
              <span v-if="errors.has('confirm')" class="label-text-alt">{{ errors.get('confirm') }}</span>
            </div>
          </div>
          <div class="grow lg:basis-1/2 h-fit p-4 border-error rounded-lg"
               :class="{'border': errors.has('roles')}">
            <UserRoleInput :model-value="modelValue.roles" @update:modelValue="onRoleUpdate"/>
            <span v-if="errors.has('roles')" class="label-text-alt">{{ errors.get('roles') }}</span>
          </div>
        </div>
        <span class="grow"></span>
        <div>
          <button class="btn m-2" @click.prevent.stop="onCancel">Annuler</button>
          <button class="btn btn-primary m-2" @click.prevent.stop="onSaveUser" :disabled="!hasValidRoles">
            Enregistrer
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

const CANCEL_EVENT: string = 'cancel';
const SUBMIT_EVENT: string = 'submit';
const CHANGE_EVENT: string = 'change';

@Component({
  name: 'UserEditor',
  components: { UserRoleInput },
  emits: [CANCEL_EVENT, SUBMIT_EVENT, CHANGE_EVENT],
})
export default class UserEditor extends Vue {
  @Prop() private modelValue: User;
  private passwordConfirm: string = '';
  private title = 'Create new user';
  private isEditionMode: boolean = false;
  private errors: Map<string, string> = new Map<string, string>();
  private opened: boolean = false;
  private closeEvent: typeof SUBMIT_EVENT | typeof CANCEL_EVENT = CANCEL_EVENT;

  mounted(): void {
    this.isEditionMode = '_id' in this.modelValue && this.modelValue._id !== undefined;
    this.title = this.isEditionMode ? `Update user ${this.modelValue.login}` : 'Create new user';
    this.$nextTick(() => this.opened = true);
  }

  private onFieldChange(field: string): void {
    this.errors.delete(field);
  }

  private onCancel(): void {
    this.closeEvent = CANCEL_EVENT;
    this.opened = false;
  }

  private onSaveUser(): void {
    if (!this.modelValue.login) {
      this.errors.set('login', 'Login is mandatory !');
    }
    if (!this.modelValue.name) {
      this.errors.set('name', 'Name is mandatory !');
    }
    if (!this.modelValue.mail) {
      this.errors.set('mail', 'Mail address is mandatory !');
    } else if (!MAIL_PATTERN.test(this.modelValue.mail)) {
      this.errors.set('mail', 'Mail address must be syntactically correct !');
    }
    if (!this.modelValue.roles || this.modelValue.roles.length === 0) {
      this.errors.set('roles', 'Role is mandatory !');
    }
    if (!this.hasValidRoles) {
      this.errors.set('roles', `All role scope must match ${ULID_PATTERN}`);
    }
    if (!this.isEditionMode) {
      if (!this.modelValue.password) {
        this.errors.set('password', 'Password is mandatory !');
      } else if (this.modelValue.password !== this.passwordConfirm) {
        this.errors.set('confirm', 'Password confirmation doesn\'t match !');
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

  get hasValidRoles(): boolean {
    return this.modelValue.roles.findIndex(r => !ULID_PATTERN.test(r)) == -1;
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