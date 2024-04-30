<template>
  <div class="md:join mb-2">
    <button v-if="actions.includes('a')" class="btn btn-sm join-item mb-2 mr-2 md:m-0"
            @click.stop.prevent="$emit('add')">
      <PlusCircleIcon class="w-6 h-6 md:mr-2"/>
      <span>Ajouter</span>
    </button>
    <button v-if="actions.includes('i')" class="btn btn-sm btn-ghost join-item mb-2 mr-2 md:m-0"
            @click="$emit('import')">
      <ArrowDownTrayIcon class="w-6 h-6 mr-2"/>
      Importer
    </button>
    <a v-if="actions.includes('e')" class="btn btn-sm btn-ghost join-item mb-2 mr-2 md:m-0" @click="$emit('export')">
      <ArrowUpTrayIcon class="w-6 h-6 mr-2"/>
      Exporter
    </a>
    <button v-if="actions.includes('l')" class="btn btn-sm btn-warning join-item"
            :disabled="selectedElements.length <= 0" @click.stop.prevent="$emit('leaveSelected', selectedElements)">
      <ArrowRightEndOnRectangleIcon class="h-6 w-6"/>
      Leave
    </button>
    <button v-if="actions.includes('d') && isGlobalEditable" class="btn btn-sm btn-error join-item mb-2 mr-2 md:m-0"
            :disabled="selectedElements.length <= 0" @click="$emit('deleteSelected', selectedElements)">
      <TrashIcon class="w-6 h-6"/>
      Supprimer
    </button>
  </div>
  <table class="table w-full table-compact" aria-describedby="User List">
    <thead>
    <tr>
      <th scope="col" class="w-1 pl-0 pr-0">
        <label v-if="isGlobalEditable">
          <input type="checkbox" class="checkbox" ref="globalCheck"
                 :checked="selectedElements.length > 0" @change="onSelectAll"/>
          <span class="checkbox-mark"></span>
        </label>
      </th>
      <th v-for="column in _columns" scope="col">{{ column }}</th>
      <th scope="col" class="text-right">
        <div class="join" v-if="totalPage > 1">
          <button class="btn btn-xs join-item hidden lg:inline" :disabled="activePage < 1"
                  @click="$emit('navigate',activePage-1)">«
          </button>
          <select class="select select-xs lg:join-item">
            <option v-for="i in totalPage" :key="i" :selected="activePage === i-1"
                    @click="$emit('navigate',i-1)">{{ i }}
            </option>
          </select>
          <button class="btn btn-xs join-item hidden lg:inline" :disabled="activePage >= totalPage-1"
                  @click="$emit('navigate',activePage+1)">»
          </button>
        </div>
      </th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="(vElement, idx) in this.elements" :key="hashCode(JSON.stringify(vElement))">
      <th scope="row" class="w-1 pl-0 pr-0">
        <label v-if="vElement.isEditable">
          <input type="checkbox" class="checkbox" v-model="vElement.isSelected">
          <span class="checkbox-mark"></span>
        </label>
      </th>
      <slot :data="vElement.data"/>
      <td>
        <div class="btn-group text-right lg:whitespace-nowrap">
          <slot name="lineActions" :idx="idx"/>
          <button v-if="actions.includes('v')" class="btn btn-sm btn-square btn-ghost"
                  @click.stop.prevent="$emit('view', idx)">
            <EyeIcon class="h-6 w-6"/>
          </button>
          <button v-if="actions.includes('u') && vElement.isEditable" class="btn btn-sm btn-square btn-ghost"
                  @click.stop.prevent="$emit('edit', idx)">
            <PencilIcon class="h-6 w-6"/>
          </button>
          <button v-if="actions.includes('l')" class="btn btn-sm btn-square btn-ghost"
                  @click.stop.prevent="$emit('leave', idx)">
            <ArrowRightEndOnRectangleIcon class="h-6 w-6"/>
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
      <th scope="col" class="text-right">
        <div class="join" v-if="totalPage > 1">
          <button class="btn btn-xs join-item hidden lg:inline" :disabled="activePage < 1"
                  @click="$emit('navigate',activePage-1)">«
          </button>
          <select class="select select-xs lg:join-item">
            <option v-for="i in totalPage" :key="i" :selected="activePage === i-1"
                    @click="$emit('navigate',i-1)">{{ i }}
            </option>
          </select>
          <button class="btn btn-xs join-item hidden lg:inline" :disabled="activePage >= totalPage-1"
                  @click="$emit('navigate',activePage+1)">»
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
  ArrowRightEndOnRectangleIcon,
  ArrowUpTrayIcon,
  EyeIcon,
  PencilIcon,
  PlusCircleIcon,
  TrashIcon,
} from '@heroicons/vue/24/outline';
import { SmartTableView } from '@/common/components/smartTable/SmartTableView.interface';

/**
 * Table component with global and line actions
 * <p>List of actions:</p>
 * <ul>
 *   <li><b>[letter: 'a', emit: 'add']</b>: it is possible to add elements</li>
 *   <li><b>[letter: 'i', emit: 'import']</b>: it is possible to batch import elements</li>
 *   <li><b>[letter: 'e', emit: 'export']</b>: it is possible to export the elements of the list</li>
 *   <li><b>[letter: 'd', emit: 'deleteSelected', 'delete']</b>: It is possible to delete one element
 *      or all selected elements</li>
 *   <li><b>[letter: 'u', emit: 'edit']</b>: it is possible to update elements</li>
 *   <li><b>[letter: 'v', emit: 'view']</b>: it is possible to view elements</li>
 *   <li><b>[letter: 'l', emit: 'leave', 'leaveSelected']</b>: it is possible to leave one or all selected elements</li>
 * </ul>
 * @requires ./SmartTableData.vue
 */
@Component({
  components: {
    ArrowDownTrayIcon,
    ArrowRightEndOnRectangleIcon,
    ArrowUpTrayIcon,
    EyeIcon,
    PencilIcon,
    PlusCircleIcon,
    TrashIcon,
  },
  emits: ['add', 'import', 'export', 'deleteSelected', 'delete', 'edit', 'view', 'leave', 'leaveSelected', 'navigate'],
  name: 'SmartTable',
})
export default class SmartTable extends Vue {
  @Prop() private elements: SmartTableView<unknown>[];
  @Prop({ default: '' }) private columns: string;
  @Prop({ default: 'ad' }) private actions: string;
  @Prop({ default: 0 }) private totalPage: number;
  @Prop({ default: 0 }) private activePage: number;

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
