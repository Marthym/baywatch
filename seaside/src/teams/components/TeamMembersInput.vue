<template>
  <div class="overflow-x-auto">
    <table class="table table-zebra table-compact">
      <caption class="label-text text-left mb-2">Manager les membres de l'équipe</caption>
      <thead>
      <tr>
        <th>Utilisateur</th>
        <th>Rôle</th>
        <th>Action</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(member, index) in members">
        <td>
          <TrophyIcon class="h-4 w-4 inline text-amber-300" v-if="member._user.roles.find(r => r === 'MANAGER')"/>
          {{ member._user.name }}
        </td>
        <td>
          {{ member.pending }}
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
import {Options, Prop, Vue} from "vue-property-decorator";
import {PlusCircleIcon, TrashIcon, TrophyIcon} from "@heroicons/vue/24/outline";
import {Member} from "@/teams/model/Member.type";
import {teamMemberList} from "@/teams/services/TeamMembers.service";

const SUBMIT_EVENT: string = 'submit';
const UPDATE_EVENT: string = 'update:modelValue';

@Options({
  name: 'TeamMembersInput',
  components: {PlusCircleIcon, TrashIcon, TrophyIcon},
  emits: [UPDATE_EVENT, SUBMIT_EVENT],
})
export default class TeamMembersInput extends Vue {
  @Prop()
  private teamId!: string;
  private members: Member[] = [];

  mounted(): void {
    teamMemberList(this.teamId).subscribe({
      next: members => this.members.splice(0, this.members.length, ...members),
    })
  }

  private onAddRole(): void {

  }

  private onRemoveRole(index: number): void {

  }

}
</script>
