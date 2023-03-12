<template>
  <div class="mx-4">
    <h3 class="font-sans text-lg border-b border-accent/40 pb-1 mb-2 w-full">Manage team members</h3>
    <table class="table table-compact lg:w-3/4 w-full" :aria-label="'members for ' + team._id">
      <thead>
      <tr>
        <th scope="col">Utilisateur</th>
        <th scope="col" class="text-center">Status</th>
        <th scope="col" class="text-right">Action</th>
      </tr>
      <tr v-if="isTeamManager">
        <td class="normal-case">
          <div class="dropdown dropdown-bottom">
            <input type="text" placeholder="Type here" class="input input-bordered input-xs w-full max-w-xs"
                   v-model="newMember._user.login"
                   @keyup="onUserKeyup"/>
            <ul tabindex="0" v-if="dropdown.length > 0"
                class="dropdown-content menu shadow bg-neutral border-primary-content border border-opacity-20 w-full">
              <li v-for="i in dropdown"><a @click="onUserDropdownClick(i)">{{ i.login }}</a></li>
            </ul>
          </div>
        </td>
        <td class="text-center">
          <component :is="getStatusComponent(newMember)" class="h-4 w-4 inline"
                     :class="{'text-amber-300': isManager(newMember)}"
                     v-if="getStatusComponent(newMember) !== undefined"/>
        </td>
        <td class="text-right">
          <button class="btn btn-sm" @click.prevent.stop="onAddRole()">
            <PlusCircleIcon class="h-6 w-6 inline"/>
          </button>
        </td>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(member, index) in members">
        <td>{{ member._user.login }}</td>
        <td class="text-center">
          <component :is="getStatusComponent(member)" class="h-4 w-4 inline"
                     :class="{'text-amber-300': isManager(member)}"
                     v-if="getStatusComponent(member) !== undefined"/>
        </td>
        <td class="text-right">
          <button v-if="isTeamManager" class="btn btn-sm" @click.prevent.stop="onRemoveMember(index)">
            <TrashIcon class="w-6 h-6 inline"/>
          </button>
        </td>
      </tr>
      </tbody>
      <tfoot>
      <tr>
        <th scope="col">Utilisateur</th>
        <th scope="col" class="text-center">Status</th>
        <th scope="col" class="text-right">Action</th>
      </tr>
      </tfoot>
    </table>
  </div>
</template>

<script lang="ts">
import {Options, Prop, Vue} from "vue-property-decorator";
import {PlusCircleIcon, TrashIcon} from "@heroicons/vue/24/outline";
import {ClockIcon, TrophyIcon, UserPlusIcon} from "@heroicons/vue/20/solid";
import {Member} from "@/teams/model/Member.type";
import {
  teamMemberAdd,
  teamMemberAvailable,
  teamMemberDelete,
  teamMemberList
} from "@/teams/services/TeamMembers.service";
import {MemberPending} from "@/teams/model/MemberPending.enum";
import {User} from "@/teams/model/User.type";
import {map} from "rxjs/operators";
import {Team} from "@/teams/model/Team.type";
import {setup} from "vue-class-component";
import {GetterTree, useStore} from "vuex";
import {UserState} from "@/store/user/user";

const SUBMIT_EVENT: string = 'submit';
const UPDATE_EVENT: string = 'update:modelValue';

@Options({
  name: 'TeamMembersInput',
  components: {PlusCircleIcon, TrashIcon, TrophyIcon, UserPlusIcon, ClockIcon},
  emits: [UPDATE_EVENT, SUBMIT_EVENT],
})
export default class TeamMembersInput extends Vue {
  @Prop() private team!: Team;
  @Prop({default: false}) private isTeamManager!: boolean;

  private newMember: Member = {pending: MemberPending.USER, _user: {}} as Member;
  private members: Member[] = [];
  private dropdown: User[] = [];
  private storeGetters: GetterTree<UserState, UserState> = setup(() => useStore().getters);

  mounted(): void {
    teamMemberList(this.team._id).subscribe({
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
        if (this.team._managers.find(m => m._id === member._user._id)) {
          return 'TrophyIcon';
        }
        return undefined;
    }
  }

  private isManager(member: Member): boolean {
    return this.team._managers.findIndex(m => m._id === member._user._id) >= 0;
  }

  private onAddRole(): void {
    teamMemberAdd(this.team._id, [this.newMember._user._id]).pipe(
        map(members => members.find(m => m._user._id === this.newMember._user._id))
    ).subscribe({
      next: member => {
        this.members.push(member);
        this.newMember = {pending: MemberPending.USER, _user: {}} as Member;
      }
    })
  }

  private onUserKeyup(event: KeyboardEvent): void {
    if (event.target && (event.target as HTMLInputElement).value.length > 1) {
      teamMemberAvailable((event.target as HTMLInputElement).value).subscribe({
        next: res => this.dropdown.splice(0, this.dropdown.length, ...res),
      });
    }
  }

  private onUserDropdownClick(u: User): void {
    this.newMember._user = u;
    this.dropdown.splice(0, this.dropdown.length);
  }

  private onRemoveMember(index: number): void {
    teamMemberDelete(this.team._id, [this.members[index]._user._id]).subscribe({
      next: () => {
        this.members.splice(index, 1);
      }
    })
  }

}
</script>
