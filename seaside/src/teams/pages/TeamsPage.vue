<template>
  <div v-if="store.getters['user/hasRoleUser']" class="overflow-x-auto mt-4">
    <SmartTable :columns="`${t('teams.header.name')}|${t('teams.header.managers')}|${t('teams.header.topic')}`" :elements="teams"
                actions="audl"
                @add="addNewTeam()"
                @delete="idx => onDeleteData(idx)"
                @deleteSelected="idx => onDeleteSelected(idx)"
                @edit="idx => onEditData(idx)"
                @leave="idx => onLeave(idx)"
                @view="idx => onEditData(idx)">
      <template #default="e">
        <std>
          {{ e.data.name }}
          <div :data-tip="e.data._id" class="tooltip tooltip-right">
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
              :icon="ArrowLeftEndOnRectangleIcon"
              class="animate-pulse text-accent" @click.stop="onJoinTeam(e.idx)"/>
      </template>
    </SmartTable>
  </div>
  <team-editor v-if="isEditorOpened" v-model="activeTeam"
               title="Team Editor" @close="onEditorClose"/>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator';
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
import { ArrowLeftEndOnRectangleIcon, InformationCircleIcon } from '@heroicons/vue/24/outline';
import { MemberPending } from '@/teams/model/MemberPending.enum';
import { teamMemberAdd, teamMemberDelete } from '@/teams/services/TeamMembers.service';
import { UserState } from '@/security/store/user';
import { useI18n } from 'vue-i18n';

@Component({
  name: 'TeamsPage',
  computed: {
    MemberPending() {
      return MemberPending;
    },
  },
  components: {
    ArrowLeftEndOnRectangleIcon,
    InformationCircleIcon,
    TeamEditor,
    std,
    stla,
    SmartTable,
  },
  setup() {
    const { t } = useI18n();
    return {
      store: useStore<UserState>(),
      t: t,
    };
  },
})
export default class TeamsPage extends Vue {
  private t;
  private isEditorOpened: boolean = false;
  private teams: SmartTableView<Team>[] = [];
  private activeTeam: SmartTableView<Team>;
  private activePage = 0;

  private store: Store<UserState>;

  private ArrowLeftEndOnRectangleIcon = ArrowLeftEndOnRectangleIcon;

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
            notificationService.pushSimpleOk(this.t('teams.messages.teamJoined', { team: this.teams[idx].data.name }));
          } else {
            notificationService.pushSimpleError(this.t('teams.messages.unknownError'));
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
    const messageConfirm = this.t('teams.messages.confirmLeaving', { team: this.teams[idx].data.name });
    const messageComplete = this.t('teams.messages.leaveSuccessfully', { team: this.teams[idx].data.name });
    const id = this.store.state.user.user._id;
    if (id && this.teams.length >= (idx - 1)) {
      this.$alert.fire(messageConfirm, AlertType.CONFIRM_DELETE, this.t('teams.leave')).pipe(
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
    const message = this.t('teams.messages.confirmDelete', { team: this.teams[idx].data.name });
    if (this.teams.length >= (idx - 1)) {
      this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
          filter(response => response === AlertResponse.CONFIRM),
          switchMap(() => teamDelete([this.teams[idx].data._id])),
      ).subscribe({
        next: deleted => {
          this.teams.splice(idx, 1);
          notificationService.pushSimpleOk(this.t('teams.messages.deletedSuccessfully', { team: deleted[0].name }));
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
            notificationService.pushSimpleOk(this.t('teams.messages.deletedSuccessfully', { team: deleted[0].name }));
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
    notificationService.pushSimpleOk(this.t('teams.messages.copiedUserClipboard'));
  }

  private unmounted(): void {
    actionServiceUnregisterFunction();
  }
}
</script>