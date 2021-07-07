<template>
  <tr>
    <th v-if="isAuthenticated">
      <label>
        <input type="checkbox" class="checkbox">
        <span class="checkbox-mark"></span>
      </label>
    </th>
    <td>
      <div class="flex items-center space-x-3">
        <div class="avatar placeholder">
          <div class="w-8 h-8 mask mask-squircle">
            <img class="text-center"
                 :src="view.icon" :alt="view.data.name.substr(0,1)"
                 @error.prevent.stop="iconFallback">
          </div>
        </div>
        <div>
          <div class="font-bold">
            {{ view.data.name }}
          </div>
          <div class="text-sm opacity-50">
            {{ view.data.id.substr(0,10) }}
          </div>
        </div>
      </div>
    </td>
    <td colspan="2">
      <a class="link">{{ view.data.url }}</a>

      <br>
<!--      <span class="badge badge-outline badge-sm">Desktop Support Technician</span>-->
    </td>
    <th v-if="isAuthenticated">
      <div class="btn-group">
        <button class="btn btn-sm btn-square btn-ghost">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
          </svg>
        </button>
        <button class="btn btn-sm btn-square btn-ghost">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
        </button>
      </div>
    </th>
  </tr>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import {FeedView} from "@/components/feedslist/model/FeedView";

@Component({
  components: {},
})
export default class FeedsListItem extends Vue {
  @Prop() private view!: FeedView;
  @Prop() private isAuthenticated!: boolean;

  iconFallback(event: ErrorEvent): void {
    (event.target as HTMLImageElement).src = '/favicon.ico'
  }
}
</script>
