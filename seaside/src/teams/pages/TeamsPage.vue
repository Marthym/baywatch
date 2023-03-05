<template>
  <div class="overflow-x-auto mt-4">
    <TableActionsComponent @add="addNewTeam"/>
    <SmartTable columns="Name|Managers|Topic" :elements="teams" v-slot="e"
                @edit="onEditData"
                @delete="onDeleteData">
      <std>{{ e.data.name }}</std>
      <std>{{ e.data._managers.map(m => m.name).join(', ') }}</std>
      <std>{{ e.data.topic }}</std>
    </SmartTable>
  </div>
  <team-editor v-if="isEditorOpened" @close="onEditorClose"
               title="Team Editor" v-model="activeTeam"/>
</template>

<script lang="ts">
import {Options, Vue} from "vue-property-decorator";
import TableActionsComponent from "@/common/components/TableActionsComponent.vue";
import SmartTable from "@/common/components/smartTable/SmartTable.vue";
import std from "@/common/components/smartTable/SmartTableData.vue";
import {SmartTableView} from "@/common/components/smartTable/SmartTableView.interface";
import {Team} from "@/teams/model/Team.type";
import {teamDelete, teamsList} from "@/teams/services/Teams.service";
import {Observable} from "rxjs";
import {map, tap} from "rxjs/operators";
import TeamEditor, {CloseEvent} from "@/teams/components/TeamEditor.vue";
import reloadActionService from "@/common/services/ReloadActionService";
import notificationService from "@/services/notification/NotificationService";

@Options({
  name: 'TeamsPage',
  components: {TeamEditor, std, SmartTable, TableActionsComponent},
})
export default class TeamsPage extends Vue {
  private isEditorOpened: boolean = false;
  private teams: SmartTableView<Team>[] = [];
  private activeTeam: SmartTableView<Team>;
  private activePage = 0;

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
    return teamsList().pipe(
        map(page => page.data),
        map(teams => teams.map(team => ({isSelected: false, data: team} as SmartTableView<Team>))),
        tap(teams => this.teams.splice(0, this.teams.length, ...teams)),
    )
  }

  private addNewTeam(): void {
    this.activeTeam = {isSelected: false, data: {} as Team};
    this.isEditorOpened = true;
  }

  private onEditData(idx): void {
    if (this.teams.length >= (idx - 1)) {
      this.activeTeam = this.teams[idx];
      this.isEditorOpened = true;
    }
  }

  private onDeleteData(idx): void {
    if (this.teams.length >= (idx - 1)) {
      teamDelete([this.teams[idx].data._id])
          .subscribe({
            next: deleted => {
              this.teams.splice(idx, 1);
              notificationService.pushSimpleOk(`${deleted[0].name} deleted successfully !`);
            },
            error: err => notificationService.pushSimpleError(err),
          })
    }
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