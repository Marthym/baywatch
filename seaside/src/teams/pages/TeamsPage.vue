<template>
  <div class="overflow-x-auto mt-4">
    <TableActionsComponent/>
    <SmartTable columns="Name|Managers|Members" :elements="teams" v-slot="e">
      <std>{{ e.data._id }}</std>
      <std>{{ e.data.name }}</std>
      <std>{{ e.data.topic }}</std>
    </SmartTable>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from "vue-property-decorator";
import TableActionsComponent from "@/common/components/TableActionsComponent.vue";
import SmartTable from "@/common/components/smartTable/SmartTable.vue";
import std from "@/common/components/smartTable/SmartTableData.vue";
import {SmartTableView} from "@/common/components/smartTable/SmartTableView.interface";
import {Team} from "@/teams/model/Team.type";

@Options({
  name: 'TeamsPage',
  components: {std, SmartTable, TableActionsComponent},
})
export default class TeamsPage extends Vue {
  private teams: SmartTableView<Team>[] = [];

  private mounted(): void {
    this.teams.push({
      isSelected: false,
      data: {
        _id: '42',
        _createdAt: '',
        _createdBy: '',
        name: 'Jedi',
        topic: 'May the 4th be with you',
      }
    });
  }
}
</script>