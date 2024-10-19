<template>
  <div class="overflow-x-auto">
    <h3 class="font-sans text-lg border-b border-accent/40 pb-1 mb-2 w-full capitalize">{{ t('admin.users.roles.title') }}</h3>
    <table :aria-label="t('admin.users.roles.title')" class="table table-zebra table-compact">
      <thead>
      <tr class="capitalize">
        <th>{{ t('admin.users.roles.name') }}</th>
        <th>{{ t('admin.users.roles.scope') }}</th>
        <th>{{ t('admin.users.roles.actions') }}</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(role, index) in roles">
        <td>
          <select v-model="role.name" class="select select-bordered select-sm max-w-xs w-32 w-full"
                  @change="emitInputEvent">
            <option class="capitalize" :value="undefined" disabled selected>{{ t('admin.users.roles.name.default') }}</option>
            <option class="capitalize" value="USER">{{ t('admin.users.roles.name.user') }}</option>
            <option class="capitalize" value="MANAGER">{{ t('admin.users.roles.name.manager') }}</option>
            <option class="capitalize" value="ADMIN">{{ t('admin.users.roles.name.admin') }}</option>
          </select>
        </td>
        <td :class="{
              'tooltip': isInvalidScope(role.scope),
              'tooltip-bottom': index < (roles.length -1),
            }"
            :data-tip="t('admin.users.roles.scope.message.invalid')" class="tooltip-error">
          <input v-model="role.scope" :class="{'input-error': isInvalidScope(role.scope)}"
                 :placeholder="t('admin.users.roles.scope.placeholder')"
                 class="input input-bordered input-sm w-72 placeholder:capitalize"
                 type="text"
                 @change="emitInputEvent"
          />
        </td>
        <td>
          <div class="join">
            <button class="btn btn-sm join-item" @click.prevent.stop="onAddRole()">
              <PlusCircleIcon class="h-6 w-6 inline"/>
            </button>
            <button class="btn btn-sm join-item" @click.prevent.stop="onRemoveRole(index)">
              <TrashIcon class="w-6 h-6 inline"/>
            </button>
          </div>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import { PlusCircleIcon, TrashIcon } from '@heroicons/vue/24/outline';
import { useI18n } from 'vue-i18n';

const SUBMIT_EVENT: string = 'submit';
const UPDATE_EVENT: string = 'update:modelValue';

@Component({
  name: 'UserRoleInput',
  components: { PlusCircleIcon, TrashIcon },
  emits: [UPDATE_EVENT, SUBMIT_EVENT],
  setup() {
    const { t } = useI18n();
    return { t };
  },
})
export default class UserRoleInput extends Vue {
  private readonly t;
  @Prop({ default: () => [] })
  private modelValue!: string[];
  private roles: RoleView[] = [];

  mounted(): void {
    this.roles = this.modelValue.map(m => {
      const roleEntity = m.split(':');
      return { name: roleEntity[0], scope: roleEntity[1] };
    });
    if (this.roles.length === 0) {
      this.roles.push({ name: '' });
    }
  }

  private onAddRole(): void {
    this.roles.push({} as RoleView);
    this.emitInputEvent();
  }

  private onRemoveRole(index: number): void {
    this.roles.splice(index, 1);
    this.emitInputEvent();
  }

  private emitInputEvent(): void {
    try {
      const value = this.roles.filter(r => r?.name).map(r => (r.scope) ? `${r.name}:${r.scope}` : r.name);
      this.$emit(UPDATE_EVENT, value);
    } catch (e) {
      console.error('emitInputEvent', e.message);
    }
  }

  private isInvalidScope(scope: string): boolean {
    return !!scope && !/^[A-Z]{2}[0-7][0-9A-HJKMNP-TV-Z]{25}$/.test(scope);
  }
}

type RoleView = {
  name: string,
  scope?: string,
}
</script>
