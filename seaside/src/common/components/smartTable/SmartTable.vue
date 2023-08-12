<template>
  <div class="md:btn-group mb-2">
    <button v-if="actions.includes('a')" class="btn btn-sm mb-2 mr-2 md:m-0"
            @click.stop.prevent="$emit('add')">
      <PlusCircleIcon class="w-6 h-6 md:mr-2"/>
      <span>Ajouter</span>
    </button>
    <button v-if="actions.includes('i')" class="btn btn-sm btn-ghost mb-2 mr-2 md:m-0" @click="$emit('import')">
      <ArrowDownTrayIcon class="w-6 h-6 mr-2"/>
      Importer
    </button>
    <a v-if="actions.includes('e')" class="btn btn-sm btn-ghost mb-2 mr-2 md:m-0" @click="$emit('export')">
      <ArrowUpTrayIcon class="w-6 h-6 mr-2"/>
      Exporter
    </a>
    <button v-if="actions.includes('l')" class="btn btn-sm btn-warning"
            :disabled="selectedElements.length <= 0" @click.stop.prevent="$emit('leaveSelected', selectedElements)">
      <ArrowRightOnRectangleIcon class="h-6 w-6"/>
      Leave
    </button>
    <button v-if="actions.includes('d') && isGlobalEditable" class="btn btn-sm btn-error mb-2 mr-2 md:m-0"
            :disabled="selectedElements.length <= 0" @click="$emit('deleteSelected', selectedElements)">
      <TrashIcon class="w-6 h-6"/>
      Supprimer
    </button>
  </div>
  <table class="table w-full table-compact" aria-describedby="User List">
    <thead>
    <tr>
      <th scope="col" class="w-1">
        <label v-if="isGlobalEditable">
          <input type="checkbox" class="checkbox" ref="globalCheck"
                 :checked="selectedElements.length > 0" @change="onSelectAll"/>
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
        <label v-if="vElement.isEditable">
          <input type="checkbox" class="checkbox" v-model="vElement.isSelected">
          <span class="checkbox-mark"></span>
        </label>
      </th>
      <slot :data="vElement.data"/>
      <td>
        <div class="btn-group justify-end w-full">
          <slot name="lineActions" :idx="idx"/>
          <button v-if="!vElement.isEditable" class="btn btn-sm btn-square btn-ghost"
                  @click.stop.prevent="$emit('view', idx)">
            <EyeIcon class="h-6 w-6"/>
          </button>
          <button v-if="vElement.isEditable" class="btn btn-sm btn-square btn-ghost"
                  @click.stop.prevent="$emit('edit', idx)">
            <PencilIcon class="h-6 w-6"/>
          </button>
          <button v-if="actions.includes('l')" class="btn btn-sm btn-square btn-ghost"
                  @click.stop.prevent="$emit('leave', idx)">
            <ArrowRightOnRectangleIcon class="h-6 w-6"/>
          </button>
          <button v-if="actions.includes('d') && vElement.isEditable" class="btn btn-sm btn-square btn-ghost"
                  @click.stop.prevent="$emit('delete', idx)">
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
import { Component, Prop, Vue } from 'vue-facing-decorator';
import {
  ArrowDownTrayIcon,
  ArrowRightOnRectangleIcon,
  ArrowUpTrayIcon,
  EyeIcon,
  PencilIcon,
  PlusCircleIcon,
  TrashIcon,
} from '@heroicons/vue/24/outline';
import { SmartTableView } from '@/common/components/smartTable/SmartTableView.interface';
import { Observable } from 'rxjs';

/**
 * Table component with global and line actions
 * @requires ./SmartTableData.vue
 */
@Component({
  components: {
    ArrowDownTrayIcon,
    ArrowRightOnRectangleIcon,
    ArrowUpTrayIcon,
    EyeIcon,
    PencilIcon,
    PlusCircleIcon,
    TrashIcon,
  },
  emits: ['add', 'import', 'export', 'deleteSelected', 'delete', 'edit', 'view', 'leave', 'leaveSelected'],
  name: 'SmartTable',
})
export default class SmartTable extends Vue {
  @Prop() private elements: SmartTableView<unknown>[];
  @Prop({ default: '' }) private columns: string;
  @Prop({ default: 'ad' }) private actions: string;
  @Prop({ default: p => console.debug(`load page ${p}, not implemented`) })
  private loadPageByIndex: (page: number) => Observable<SmartTableView<unknown>[]>;

  private pagesNumber = 0;
  private activePage = 0;

  get _columns(): string[] {
    return this.columns.split('|');
  }

  get isGlobalEditable(): boolean {
    return this.elements.findIndex(e => e.isEditable) >= 0;
  }

  get selectedElements(): number[] {
    const selected: number[] = [];
    for (let i = 0; i < this.elements.length; i++) {
      if (this.elements[i].isSelected) {
        selected.push(i);
      }
    }
    if (this.$refs?.['globalCheck']) {
      this.$refs['globalCheck'].indeterminate = (selected.length > 0) && this.elements.find(t => !t.isSelected) !== undefined;
    }
    return selected;
  }

  private onSelectAll(event: InputEvent): void {
    const target: HTMLInputElement = event.target as HTMLInputElement;
    const current = target.checked;
    this.elements.forEach(f => f.isSelected = current);
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
