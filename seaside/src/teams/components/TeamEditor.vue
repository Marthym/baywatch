<template>
  <curtain-modal v-slot="curtainModal" @leave="close()">
    <h2 class="font-sans text-xl border-b border-accent/40 pb-2 w-full">{{ title }}</h2>
    <div class="m-4">
      <label class="label">
        <span class="label-text">Team Name</span>
      </label>
      <input v-model="value.data!.name" :class="{'input-error': errors.has('login')}" :disabled="!value.isEditable"
             class="input input-bordered w-full"
             type="text">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('name')" class="label-text-alt">{{ errors.get('name') }}</span>
      </label>
      <label class="label">
        <span class="label-text">Team Topic</span>
      </label>
      <input v-model="value.data!.topic" :class="{'input-error': errors.has('topic')}" :disabled="!value.isEditable"
             class="input input-bordered w-full"
             type="text">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('topic')" class="label-text-alt">{{ errors.get('topic') }}</span>
      </label>
      <div class="text-right">
        <button class="btn btn-sm mx-1" @click.stop="curtainModal.close()">Cancel</button>
        <button v-if="value.isEditable" class="btn btn-sm btn-primary mx-1" @click.stop="throttledOnSave">
          Save
        </button>
      </div>
    </div>
    <team-members-input v-if="isMembersDisplayable()"
                        :isTeamManager="value.isEditable" :team="value.data"/>
  </curtain-modal>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import CurtainModal from '@/common/components/CurtainModal.vue';
import { Team } from '@/teams/model/Team.type';
import { teamCreate, teamUpdate } from '@/teams/services/Teams.service';
import { SmartTableView } from '@/common/components/smartTable/SmartTableView.interface';
import throttle from 'lodash/throttle';
import TeamMembersInput from '@/teams/components/TeamMembersInput.vue';
import notificationService from '@/services/notification/NotificationService';
import { USER_ADD_ROLE_MUTATION } from '@/security/store/UserConstants';
import { Store, useStore } from 'vuex';

const CLOSE_EVENT = 'close';
const UPDATE_EVENT = 'update:model-value';

@Component({
  name: 'TeamEditor',
  components: { CurtainModal, TeamMembersInput },
  emits: [CLOSE_EVENT, UPDATE_EVENT],
  setup() {
    const store = useStore();
    return { store };
  },
})
export default class TeamEditor extends Vue {
  @Prop({ default: 'Edit Team' }) private title!: string;
  @Prop() private modelValue!: SmartTableView<Team>;

  private readonly store!: Store<unknown>;
  private errors: Map<string, string> = new Map<string, string>();
  private payload: CloseEvent = {
    updated: false,
  };
  private throttledOnSave: () => void;

  get value(): SmartTableView<Team> {
    return this.modelValue;
  }

  set value(v: SmartTableView<Team>) {
    this.$emit(UPDATE_EVENT, v);
  }

  private isMembersDisplayable(): boolean {
    return (this.value.data !== undefined) && '_id' in this.value.data;
  }

  /**
   * @see unmounted
   */
  private unmounted(): void {
    delete this.throttledOnSave;
  }

  private onSave(): void {
    if (!this.value.data) {
      console.error('No data to persist !');
    } else if ('_id' in this.value.data) {
      teamUpdate(this.value.data._id, this.value.data)
          .subscribe({
            next: team => {
              Object.assign(this.value.data, team);
              this.payload.updated = true;
              notificationService.pushSimpleOk(`Team ${team.name} updated successfully !`);
            },
          });
    } else {
      teamCreate(this.value.data.name, this.value.data.topic)
          .subscribe({
            next: team => {
              Object.assign(this.value.data, team);
              this.payload.updated = true;
              store.commit(USER_ADD_ROLE_MUTATION, `MANAGER:${team._id}`);
              notificationService.pushSimpleOk(`Team ${team.name} saved successfully !`);
            },
          });
    }
  }

  /**
   * @see created
   */
  private created(): void {
    this.throttledOnSave = throttle(this.onSave, 1000, { 'trailing': false });
  }

  private close(): void {
    this.$emit(CLOSE_EVENT, this.payload);
  }
}

export type CloseEvent = {
  updated: boolean,
}
</script>
