<template>
  <div class="overflow-x-auto mt-4">
    <SmartTable columns="Name|Managers|Topic" :elements="teams" actions="adl"
                @add="addNewTeam"
                @view="onEditData"
                @edit="onEditData"
                @delete="onDeleteData"
                @deleteSelected="onDeleteSelected">
      <template #default="e">
        <std>{{ e.data.name }}</std>
        <std>{{ e.data._managers.map(m => m.name).join(', ') }}</std>
        <std>{{ e.data.topic }}</std>
      </template>
      <template #lineActions="e">
        <stla v-if="this.teams[e.idx].data._me.pending === MemberPending.USER"
              class="animate-pulse text-accent"
              :icon="ArrowLeftOnRectangleIcon" @click.stop="onJoinTeam(e.idx)"/>
      </template>
    </SmartTable>
  </div>
  <team-editor v-if="isEditorOpened" @close="onEditorClose"
               title="Team Editor" v-model="activeTeam"/>
</template>

<script lang="ts">
import {Options, Vue} from "vue-property-decorator";
import TableActionsComponent from "@/common/components/TableActionsComponent.vue";
import SmartTable from "@/common/components/smartTable/SmartTable.vue";
import stla from '@/common/components/smartTable/SmartTableLineAction.vue'
import std from "@/common/components/smartTable/SmartTableData.vue";
import {SmartTableView} from "@/common/components/smartTable/SmartTableView.interface";
import {Team} from "@/teams/model/Team.type";
import {teamDelete, teamsList} from "@/teams/services/Teams.service";
import {Observable} from "rxjs";
import {filter, map, switchMap, tap} from "rxjs/operators";
import TeamEditor, {CloseEvent} from "@/teams/components/TeamEditor.vue";
import reloadActionService from "@/common/services/ReloadActionService";
import notificationService from "@/services/notification/NotificationService";
import {useStore} from "vuex";
import {setup} from "vue-class-component";
import {NotificationCode} from "@/services/notification/NotificationCode.enum";
import {Severity} from "@/services/notification/Severity.enum";
import {AlertResponse, AlertType} from "@/common/components/alertdialog/AlertDialog.types";
import {ArrowLeftOnRectangleIcon} from '@heroicons/vue/24/outline';
import {MemberPending} from "@/teams/model/MemberPending.enum";
import {teamMemberAdd} from "@/teams/services/TeamMembers.service";
import {UserState} from "@/store/user/user";

@Options({
  name: 'TeamsPage',
  computed: {
    MemberPending() {
      return MemberPending
    }
  },
  methods: {ArrowLeftOnRectangleIcon},
  components: {TeamEditor, std, stla, SmartTable, TableActionsComponent},
})
export default class TeamsPage extends Vue {
  private isEditorOpened: boolean = false;
  private teams: SmartTableView<Team>[] = [];
  private activeTeam: SmartTableView<Team>;
  private activePage = 0;
  private userState: UserState = setup(() => useStore().state.user);

  // noinspection JSUnusedLocalSymbols
  private mounted(): void {
    this.loadNextPage().subscribe({
      next: () => {
        reloadActionService.registerReloadFunction(context => {
          if (context === '' || context === 'teams') {
            this.loadNextPage(this.activePage).subscribe();
          }
        })
      }
    });
  }

  private loadNextPage(page: number = 0): Observable<SmartTableView<Team>[]> {
    const roles: string[] = this.userState.user.roles;
    return teamsList(page).pipe(
        map(page => page.data),
        map(teams => teams.map(team => {
          const isEditable = roles.includes(`MANAGER:${team._id}`);
          return {isSelected: false, isEditable: isEditable, data: team} as SmartTableView<Team>;
        })),
        tap(teams => this.teams.splice(0, this.teams.length, ...teams)),
    )
  }

  private addNewTeam(): void {
    this.activeTeam = {isSelected: false, isEditable: true, data: {} as Team};
    this.isEditorOpened = true;
  }

  private onJoinTeam(idx): void {
    const id = this.userState.user._id;
    if (id) {
      teamMemberAdd(this.teams[idx].data._id, [id]).subscribe({
        next: members => {
          const meAsMember = members.find(m => m._user._id === id);
          if (meAsMember && meAsMember.pending === MemberPending.NONE) {
            this.teams[idx].data._me.pending = meAsMember.pending;
            notificationService.pushSimpleOk(`You join the team ${this.teams[idx].data.name} !`);
          } else {
            notificationService.pushSimpleError('Unknown error !');
          }
        },
      })
    }
  }

  private onEditData(idx): void {
    if (this.teams.length >= (idx - 1)) {
      this.activeTeam = this.teams[idx];
      this.isEditorOpened = true;
    }
  }

  private onDeleteData(idx: number): void {
    const message = `Remove the team <b>${this.teams[idx].data.name}</b> ?`;
    if (this.teams.length >= (idx - 1)) {
      this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
          filter(response => response === AlertResponse.CONFIRM),
          switchMap(() => teamDelete([this.teams[idx].data._id])),
      ).subscribe({
        next: deleted => {
          this.teams.splice(idx, 1);
          notificationService.pushSimpleOk(`${deleted[0].name} deleted successfully !`);
        },
        error: err => notificationService.pushNotification({
          code: NotificationCode.ERROR,
          severity: Severity.error,
          message: err.message,
        }),
      })
    }
  }

  private onDeleteSelected(idx: number[]): void {
    const toBeDeleted: string[] = idx.map(i => this.teams[i].data._id);
    teamDelete(toBeDeleted)
        .subscribe({
          next: deleted => {
            idx.forEach(i => this.teams.splice(i, 1));
            notificationService.pushSimpleOk(`${deleted[0].name} deleted successfully !`);
          },
          error: err => notificationService.pushNotification({
            code: NotificationCode.ERROR,
            severity: Severity.error,
            message: err.message,
          }),
        });
  }

  private onEditorClose(event: CloseEvent): void {
    this.isEditorOpened = false;
    if (event.updated) {
      this.loadNextPage(this.activePage).subscribe();
    }
  }

  // noinspection JSUnusedLocalSymbols
  private unmounted(): void {
    reloadActionService.unregisterReloadFunction();
  }
}
</script>