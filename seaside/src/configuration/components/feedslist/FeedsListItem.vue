<template>
  <FeedCard :view="{...view.data, icon: view.icon}"/>
  <div class="md:col-span-2 btn-group justify-self-end">
    <button class="btn btn-sm btn-square btn-ghost" @click="$emit('item-update', view.data)">
      <PencilIcon class="h-6 w-6"/>
    </button>
    <button class="btn btn-sm btn-square btn-ghost" @click="$emit('item-delete', view.data.id)">
      <TrashIcon class="h-6 w-6"/>
    </button>
  </div>
</template>

<script lang="ts">
import {Options, Prop, Vue} from 'vue-property-decorator';
import {FeedView} from "@/configuration/components/feedslist/model/FeedView";
import FeedCard from "@/common/components/FeedCard.vue";
import {PencilIcon, TrashIcon} from "@heroicons/vue/24/outline";

@Options({
  name: 'FeedsListItem',
  components: {FeedCard, PencilIcon, TrashIcon},
  emits: ['item-update', 'item-delete']
})
export default class FeedsListItem extends Vue {
  @Prop() private view!: FeedView;

}
</script>
