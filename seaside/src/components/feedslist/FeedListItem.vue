<template>
  <tr>
    <th>
      <label>
        <input type="checkbox" class="checkbox">
        <span class="checkbox-mark"></span>
      </label>
    </th>
    <td>
      <div class="flex items-center space-x-3">
        <div class="avatar">
          <div class="w-12 h-12 mask mask-squircle">
            <img src="/tailwind-css-component-profile-2@56w.png" alt="Avatar Tailwind CSS Component">
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
    <td>
      Zemlak, Daniel and Leannon

      <br>
      <span class="badge badge-outline badge-sm">Desktop Support Technician</span>
    </td>
    <td>Purple</td>
    <th>
      <button class="btn btn-ghost btn-xs">details</button>
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
}
</script>
