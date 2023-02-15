<template>
  <table class="table w-full table-compact" aria-describedby="User List">
    <thead>
    <tr>
      <th scope="col" class="w-1">
        <label>
          <input type="checkbox" class="checkbox" ref="globalCheck"
                 :checked="checkState" @change="onSelectAll()"/>
          <span class="checkbox-mark"></span>
        </label>
      </th>
      <th v-for="column in _columns" scope="col">{{ column }}</th>
      <th scope="col">
        <div class="btn-group justify-end" v-if="pagesNumber > 1">
          <button v-for="i in pagesNumber" :key="i"
                  :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
                  v-on:click="loadPageByIndex(i-1).subscribe()">
            {{ i }}
          </button>
        </div>
      </th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="(vElement, idx) in this.elements" :key="hashCode(JSON.stringify(vElement))">
      <th scope="row" class="w-1">
        <label>
          <input type="checkbox" class="checkbox" v-model="vElement.isSelected">
          <span class="checkbox-mark"></span>
        </label>
      </th>
      <slot :data="vElement.data"/>
      <td>
        <div class="btn-group justify-end w-full">
          <button class="btn btn-sm btn-square btn-ghost" @click.stop.prevent="$emit('edit', idx)">
            <PencilIcon class="h-6 w-6"/>
          </button>
          <button class="btn btn-sm btn-square btn-ghost" @click.stop.prevent="$emit('delete', idx)">
            <TrashIcon class="h-6 w-6"/>
          </button>
        </div>
      </td>
    </tr>
    </tbody>
    <tfoot>
    <tr>
      <th scope="col" class="w-1"></th>
      <th v-for="column in _columns" scope="col">{{ column }}</th>
      <th scope="col">
        <div class="btn-group justify-end" v-if="pagesNumber > 1">
          <button v-for="i in pagesNumber" :key="i"
                  :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
                  v-on:click="loadPageByIndex(i-1).subscribe()">
            {{ i }}
          </button>
        </div>
      </th>
    </tr>
    </tfoot>
  </table>
</template>

<script lang="ts">
import {Options, Prop, Vue} from "vue-property-decorator";
import {ArrowDownTrayIcon, ArrowUpTrayIcon, PencilIcon, PlusCircleIcon, TrashIcon} from "@heroicons/vue/24/outline";
import {SmartTableView} from "@/common/components/smartTable/SmartTableView.interface";
import {Observable} from "rxjs";

@Options({
  name: 'SmartTable',
  components: {ArrowDownTrayIcon, ArrowUpTrayIcon, PencilIcon, PlusCircleIcon, TrashIcon},
  emits: [],
})
export default class SmartTable extends Vue {
  @Prop()
  private elements: SmartTableView[];
  @Prop({default: ""})
  private columns: string;
  @Prop({default: p => console.log(`load page ${p}`)})
  private loadPageByIndex: (page: number) => Observable<SmartTableView[]>;

  private pagesNumber = 0;
  private activePage = 0;

  get _columns(): string[] {
    return this.columns.split('|');
  }

  get checkState(): boolean {
    const teamView: SmartTableView | undefined = this.elements.find(t => t.isSelected);
    const isOneSelected = teamView !== undefined;
    if (this.$refs && this.$refs['globalCheck'])
      this.$refs['globalCheck'].indeterminate = isOneSelected && this.elements.find(t => !t.isSelected) !== undefined;
    return isOneSelected;
  }

  private onSelectAll(): void {
    const current = this.checkState;
    this.elements.forEach(f => f.isSelected = !current);
  }

  private hashCode(data: string): number {
    let hash = 0;
    for (let i = 0; i < data.length; i++) {
      let char = data.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash;
    }
    return hash;
  }
}
</script>

<style scoped>

</style>