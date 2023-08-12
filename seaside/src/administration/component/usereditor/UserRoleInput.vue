<template>
  <div class="overflow-x-auto">
    <h3 class="font-sans text-lg border-b border-accent/40 pb-1 mb-2 w-full">User role(s)</h3>
    <table class="table table-zebra table-compact" aria-label="User role(s)">
      <thead>
      <tr>
        <th>Name</th>
        <th>Scope</th>
        <th>Action</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(role, index) in roles">
        <td>
          <select v-model="role.name" class="select select-bordered select-sm max-w-xs w-32"
                  @change="emitInputEvent">
            <option :value="undefined" disabled selected hidden>Choose the user role</option>
            <option>USER</option>
            <option>MANAGER</option>
            <option>ADMIN</option>
          </select>
        </td>
        <td class="tooltip-error"
            :class="{
              'tooltip': isInvalidScope(role.scope),
              'tooltip-bottom': index < (roles.length -1),
            }" data-tip="Invalid role scope !">
          <input v-model="role.scope" type="text" placeholder="Type here"
                 @change="emitInputEvent"
                 class="input input-bordered input-sm w-72"
                 :class="{'input-error': isInvalidScope(role.scope)}"
          />
        </td>
        <td>
          <div class="btn-group">
            <button class="btn btn-sm" @click.prevent.stop="onAddRole()">
              <PlusCircleIcon class="h-6 w-6 inline"/>
            </button>
            <button class="btn btn-sm" @click.prevent.stop="onRemoveRole(index)">
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

const SUBMIT_EVENT: string = 'submit';
const UPDATE_EVENT: string = 'update:modelValue';

@Component({
  name: 'UserRoleInput',
  components: { PlusCircleIcon, TrashIcon },
  emits: [UPDATE_EVENT, SUBMIT_EVENT],
})
export default class UserRoleInput extends Vue {
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
