<template>
  <div v-if="store.getters['user/hasRoleUser']" class="overflow-x-auto mt-4">
    <SmartTable columns="Name|Managers|Topic" :elements="teams" actions="adl"
                @add="addNewTeam()"
                @view="idx => onEditData(idx)"
                @edit="idx => onEditData(idx)"
                @delete="idx => onDeleteData(idx)"
                @leave="idx => onLeave(idx)"
                @deleteSelected="idx => onDeleteSelected(idx)">
      <template #default="e">
        <std>
          {{ e.data.name }}
          <div class="tooltip tooltip-right" :data-tip="e.data._id">
            <button class="btn btn-circle btn-xs btn-ghost -ml-2"
                    @click.prevent.stop="onCopyToClipboard(e.data._id)">
              <InformationCircleIcon class="w-3 h-3"/>
            </button>
          </div>
        </std>
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
import { Component, Vue } from 'vue-facing-decorator';
import TableActionsComponent from '@/common/components/TableActionsComponent.vue';
import SmartTable from '@/common/components/smartTable/SmartTable.vue';
import stla from '@/common/components/smartTable/SmartTableLineAction.vue';
import std from '@/common/components/smartTable/SmartTableData.vue';
import { SmartTableView } from '@/common/components/smartTable/SmartTableView.interface';
import { Team } from '@/teams/model/Team.type';
import { teamDelete, teamsList } from '@/teams/services/Teams.service';
import { Observable } from 'rxjs';
import { filter, map, switchMap, tap } from 'rxjs/operators';
import TeamEditor, { CloseEvent } from '@/teams/components/TeamEditor.vue';
import { actionServiceRegisterFunction, actionServiceUnregisterFunction } from '@/common/services/ReloadActionService';
import notificationService from '@/services/notification/NotificationService';
import { Store, useStore } from 'vuex';
import { NotificationCode } from '@/services/notification/NotificationCode.enum';
import { Severity } from '@/services/notification/Severity.enum';
import { AlertResponse, AlertType } from '@/common/components/alertdialog/AlertDialog.types';
import { ArrowLeftOnRectangleIcon, InformationCircleIcon } from '@heroicons/vue/24/outline';
import { MemberPending } from '@/teams/model/MemberPending.enum';
import { teamMemberAdd, teamMemberDelete } from '@/teams/services/TeamMembers.service';
import { UserState } from '@/store/user/user';

@Component({
  name: 'TeamsPage',
  computed: {
    MemberPending() {
      return MemberPending;
    },
  },
  components: {
    ArrowLeftOnRectangleIcon,
    InformationCircleIcon,
    TeamEditor,
    std,
    stla,
    SmartTable,
    TableActionsComponent,
  },
  setup() {
    return {
      store: useStore<UserState>(),
    };
  },
})
export default class TeamsPage extends Vue {
  private isEditorOpened: boolean = false;
  private teams: SmartTableView<Team>[] = [];
  private activeTeam: SmartTableView<Team>;
  private activePage = 0;

  private store: Store<UserState>;

  private ArrowLeftOnRectangleIcon = ArrowLeftOnRectangleIcon;

  /**
   * @see mounted
   */
  private mounted(): void {
    this.loadNextPage().subscribe({
      next: () => {
        actionServiceRegisterFunction(context => {
          if (context === '' || context === 'teams') {
            this.loadNextPage(this.activePage).subscribe();
          }
        });
      },
    });
  }

  public loadNextPage(page: number = 0): Observable<SmartTableView<Team>[]> {
    const roles: string[] = this.store.state.user.user.roles;
    return teamsList(page).pipe(
        switchMap(page => page.data),
        map(teams => teams.map(team => {
          const isEditable = roles.includes(`MANAGER:${team._id}`);
          return { isSelected: false, isEditable: isEditable, data: team } as SmartTableView<Team>;
        })),
        tap(teams => this.teams.splice(0, this.teams.length, ...teams)),
    );
  }

  private addNewTeam(): void {
    this.activeTeam = { isSelected: false, isEditable: true, data: {} as Team };
    this.isEditorOpened = true;
  }

  private onJoinTeam(idx): void {
    const id = this.store.state.user.user._id;
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
      });
    }
  }

  private onEditData(idx): void {
    if (this.teams.length >= (idx - 1)) {
      this.activeTeam = this.teams[idx];
      this.isEditorOpened = true;
    }
  }

  private onLeave(idx: number): void {
    const messageConfirm = `You will leave the team <strong>${this.teams[idx].data.name}</strong> definitively ?`;
    const messageComplete = `You have left the team ${this.teams[idx].data.name} ?`;
    const id = this.store.state.user.user._id;
    if (id && this.teams.length >= (idx - 1)) {
      this.$alert.fire(messageConfirm, AlertType.CONFIRM_DELETE, 'Leave').pipe(
          filter(response => response === AlertResponse.CONFIRM),
          switchMap(() => teamMemberDelete(this.teams[idx].data._id, [id])),
      ).subscribe({
        next: () => {
          this.teams.splice(idx, 1);
          notificationService.pushSimpleOk(messageComplete);
        },
        error: err => notificationService.pushNotification({
          code: NotificationCode.ERROR,
          severity: Severity.error,
          message: err.message,
        }),
      });
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
      });
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

  private onCopyToClipboard(value: string): void {
    navigator.clipboard.writeText(value);
    notificationService.pushSimpleOk(`User ID copied on clipboard !`);
  }

  // noinspection JSUnusedLocalSymbols
  private unmounted(): void {
    actionServiceUnregisterFunction();
  }
}
</script>