<template>
  <div>
    <table class="table table-zebra table-compact">
      <caption class="label-text text-left mb-2">Gérer les membres de l'équipe</caption>
      <thead>
      <tr>
        <th>Utilisateur</th>
        <th>Status</th>
        <th class="text-center">Action</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(member, index) in members">
        <td v-if="member._id">{{ member._user.login }}</td>
        <td v-else>
          <div class="dropdown dropdown-bottom">
            <input type="text" placeholder="Type here" class="input input-bordered input-xs w-full max-w-xs"
                   v-model="member._user.login"
                   @keyup="onUserKeyup"/>
            <ul tabindex="0" v-if="dropdown.length > 0"
                class="dropdown-content menu shadow bg-neutral border-primary-content border border-opacity-20 w-full">
              <li v-for="i in dropdown"><a @click="onUserDropdownClick(index, i)">{{ i.login }}</a></li>
            </ul>
          </div>
        </td>
        <td class="text-center">
          <component :is="getStatusComponent(member)" class="h-4 w-4 inline text-amber-300"
                     v-if="getStatusComponent(member) !== undefined"/>
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
import {PlusCircleIcon, TrashIcon} from "@heroicons/vue/24/outline";
import {ClockIcon, TrophyIcon, UserPlusIcon} from "@heroicons/vue/20/solid";
import {Member} from "@/teams/model/Member.type";
import {teamMemberAvailable, teamMemberList} from "@/teams/services/TeamMembers.service";
import {MemberPending} from "@/teams/model/MemberPending.enum";
import {User} from "@/teams/model/User.type";

const SUBMIT_EVENT: string = 'submit';
const UPDATE_EVENT: string = 'update:modelValue';

@Options({
  name: 'TeamMembersInput',
  components: {PlusCircleIcon, TrashIcon, TrophyIcon, UserPlusIcon, ClockIcon},
  emits: [UPDATE_EVENT, SUBMIT_EVENT],
})
export default class TeamMembersInput extends Vue {
  @Prop()
  private teamId!: string;
  private members: Member[] = [];
  private dropdown: User[] = [];

  mounted(): void {
    teamMemberList(this.teamId).subscribe({
      next: members => this.members.splice(0, this.members.length, ...members),
    })
  }

  private getStatusComponent(member: Member): string | undefined {
    switch (member.pending) {
      case MemberPending.MANAGER:
        return 'UserPlus';
      case MemberPending.USER:
        return 'ClockIcon';
      case MemberPending.NONE:
      default:
        if (member._user.roles.find(r => r === 'MANAGER')) {
          return 'TrophyIcon';
        }
        return undefined;
    }
  }

  private onAddRole(): void {
    this.members.push({_user: {}, pending: MemberPending.USER} as Member);
  }

  private onUserKeyup(event: KeyboardEvent): void {
    if (event.target && (event.target as HTMLInputElement).value.length > 1) {
      teamMemberAvailable((event.target as HTMLInputElement).value).subscribe({
        next: res => this.dropdown.splice(0, this.dropdown.length, ...res),
      });
    }
  }

  private onUserDropdownClick(idx: number, u: User): void {
    this.members[idx]._user = u;
    this.dropdown.splice(0, this.dropdown.length);
  }

  private onRemoveRole(index: number): void {

  }

}
</script>
