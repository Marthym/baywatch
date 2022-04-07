<template>
  <ul class="pt-8">
    <li>
      <div class="flex">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
        </svg>
        <span class="ml-2 capitalize font-medium">tags</span>
      </div>

      <ul class="flex flex-wrap list-none mt-4">
        <li v-for="tag in tags" v-bind:key="tag">
          <button class="badge m-1" :class="{'badge-accent': selected && tag === selected}" @click="selectTag">{{
              tag
            }}
          </button>
        </li>
      </ul>
    </li>
  </ul>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';
import tagsService from "@/services/TagsService";
import {setup} from "vue-class-component";
import {useRouter} from "vue-router";

@Options({name: 'SideNavTags'})
export default class SideNavTags extends Vue {
  private router = setup(() => useRouter());
  private tags: string[] = [];
  private selected = '';

  mounted(): void {
    tagsService.list().subscribe({
      next: tags => {
        this.tags = tags;
        const queryTag: string = this.router.currentRoute.query.tag as string;
        if (queryTag && this.tags.indexOf(queryTag) >= 0) {
          this.selected = queryTag;
        }
      }
    });
  }

  selectTag(event: MouseEvent): void {
    const selected = (event.target as HTMLElement).innerText;
    if (this.selected === selected) {
      this.selected = tagsService.select('');
      this.router.replace({ path: this.router.currentRoute.path })
    } else {
      this.selected = tagsService.select(selected);
      this.router.replace({ path: this.router.currentRoute.path, query: { tag: selected } })
    }
  }
}
</script>