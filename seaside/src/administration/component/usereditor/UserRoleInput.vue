<template>
  <div class="overflow-x-auto">
    <table class="table table-zebra table-compact">
      <caption class="label-text text-left mb-2">Rôle(s) de l'utilisateur</caption>
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
          <select v-model="role.name" class="select select-bordered select-sm max-w-xs" @change="emitInputEvent">
            <option :value="undefined" disabled selected hidden>Choose the user role</option>
            <option>USER</option>
            <option>MANAGER</option>
            <option>ADMIN</option>
          </select>
        </td>
        <td><input v-model="role.scope" type="text" placeholder="Type here" @change="emitInputEvent"
                   class="input input-bordered input-sm w-full"/></td>
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
import {Options, Prop, Vue} from "vue-property-decorator";
import {PlusCircleIcon, TrashIcon} from "@heroicons/vue/24/outline";

const SUBMIT_EVENT: string = 'submit';
const UPDATE_EVENT: string = 'update:modelValue';

@Options({
  name: 'UserRoleInput',
  components: {PlusCircleIcon, TrashIcon},
  emits: [UPDATE_EVENT, SUBMIT_EVENT],
})
export default class UserRoleInput extends Vue {
  @Prop({default: () => []})
  private modelValue!: string[];
  private roles: RoleView[] = [];

  mounted(): void {
    this.roles = this.modelValue.map(m => {
      const roleEntity = m.split(':');
      return {name: roleEntity[0], scope: roleEntity[1]};
    });
    if (this.roles.length === 0) {
      this.roles.push({name: ""});
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
    this.$emit('update:modelValue', this.roles.filter(r => r && r.name).map(r => (r.scope) ? `${r.name}:${r.scope}` : r.name));
  }

  private emitSubmitEvent(): void {
    this.$emit('submit');
  }
}

type RoleView = {
  name: string,
  scope?: string,
}
</script>
