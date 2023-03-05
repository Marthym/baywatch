<template>
  <curtain-modal @leave="close()" v-slot="scope">
    <h2 class="font-sans text-xl border-b border-accent/40 pb-2 w-full">{{ title }}</h2>
    <div class="m-4">
      <label class="label">
        <span class="label-text">Team Name</span>
      </label>
      <input v-model="modelValue.data.name" type="text" class="input input-bordered w-full"
             :class="{'input-error': errors.has('login')}">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('name')" class="label-text-alt">{{ errors.get('name') }}</span>
      </label>
      <label class="label">
        <span class="label-text">Team Topic</span>
      </label>
      <input v-model="modelValue.data.topic" type="text" class="input input-bordered w-full"
             :class="{'input-error': errors.has('topic')}">
      <label class="label -mt-1">
        <span class="label-text-alt">&nbsp;</span>
        <span v-if="errors.has('topic')" class="label-text-alt">{{ errors.get('topic') }}</span>
      </label>
      <div class="text-right">
        <button class="btn" @click.stop="throttledOnSave">Save</button>
      </div>
    </div>
  </curtain-modal>
</template>

<script lang="ts">
import {Options, Prop, Vue} from "vue-property-decorator";
import CurtainModal from "@/common/components/CurtainModal.vue";
import {Team} from "@/teams/model/Team.type";
import {teamCreate, teamUpdate} from "@/teams/Teams.service";
import notificationService from "@/services/notification/NotificationService";
import {SmartTableView} from "@/common/components/smartTable/SmartTableView.interface";
import * as throttle from "lodash/throttle";

const CLOSE_EVENT = 'close';

@Options({
  name: 'TeamEditor',
  components: {CurtainModal},
  emits: [CLOSE_EVENT]
})
export default class TeamEditor extends Vue {
  @Prop({default: 'Edit Team'})
  private title!: string;

  @Prop()
  private modelValue!: SmartTableView<Team>;

  private errors: Map<string, string> = new Map<string, string>();
  private payload: CloseEvent = {
    updated: false,
  };
  private throttledOnSave: () => void;

  /**
   * @see created
   */
  private created(): void {
    this.throttledOnSave = throttle(this.onSave, 1000, {'trailing': false});
  }

  /**
   * @see unmounted
   */
  private unmounted(): void {
    delete this.throttledOnSave;
  }

  private onSave(): void {
    let mv = this.modelValue;
    if (!mv.data) {
      console.error('No data to persist !');
    } else if ('_id' in mv.data) {
      teamUpdate(mv.data._id, mv.data)
          .subscribe({
            next: team => {
              mv.data = team;
              this.payload.updated = true;
              notificationService.pushSimpleOk(`Team ${team.name} updated successfully !`);
            },
          })
    } else {
      teamCreate(mv.data.name, mv.data.topic)
          .subscribe({
            next: team => {
              mv.data = team;
              this.payload.updated = true;
              notificationService.pushSimpleOk(`Team ${team.name} saved successfully !`);
            }
          });
    }
  }

  private close(): void {
    this.$emit(CLOSE_EVENT, this.payload);
  }
}

export type CloseEvent = {
  updated: boolean,
}
</script>
