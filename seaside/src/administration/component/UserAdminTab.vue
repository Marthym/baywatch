<template>
  <div class="overflow-x-auto mt-4">
    <div class="md:btn-group mb-2">
      <button class="btn btn-sm btn-primary mb-2 mr-2 md:m-0" @click.prevent="editorOpened = true">
        <svg class="w-6 h-6 md:mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
             xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
        <span>Ajouter</span>
      </button>
      <button class="btn btn-sm btn-primary mb-2 mr-2 md:m-0" @click="">
        <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
             xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
        </svg>
        Importer
      </button>
      <a class="btn btn-sm btn-primary mb-2 mr-2 md:m-0">
        <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
             xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path>
        </svg>
        Exporter
      </a>
      <button class="btn btn-sm btn-error mb-2 mr-2 md:m-0" :disabled="!deleteEnable" @click="">
        <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
          <path fill-rule="evenodd"
                d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0
                100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0
                102 0V8a1 1 0 00-1-1z"
                clip-rule="evenodd"></path>
        </svg>
        Suprimer
      </button>
    </div>
    <table class="table w-full table-compact">
      <thead>
      <tr>
        <th>
          <label>
            <input type="checkbox" class="checkbox" ref="globalCheck"
                   :checked="checkState"/>
            <span class="checkbox-mark"></span>
          </label>
        </th>
        <th>Pseudo</th>
        <th>Nom</th>
        <th>Mail</th>
        <th>Role</th>
        <th>Last Login
          <div class="btn-group justify-end" v-if="pagesNumber > 1">
            <button v-for="i in pagesNumber" :key="i"
                    :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
                    v-on:click="loadUserPage(i-1).subscribe()">
              {{ i }}
            </button>
          </div>
        </th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="vUser in this.users" v-bind:key="vUser.data.id">
        <th>
          <label>
            <input type="checkbox" class="checkbox" v-model="vUser.isSelected">
            <span class="checkbox-mark"></span>
          </label>
        </th>
        <td>{{ vUser.data.login }}</td>
        <td>{{ vUser.data.name }}</td>
        <td>{{ vUser.data.mail }}</td>
        <td>{{ vUser.data.role }}</td>
        <td>{{ dateToString(vUser.data._createdAt) }}</td>
      </tr>
      </tbody>
      <tfoot>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>company</th>
        <th>location</th>
        <th>Last Login
          <div class="btn-group justify-end" v-if="pagesNumber > 1">
            <button v-for="i in pagesNumber" :key="i"
                    :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
                    v-on:click="loadUserPage(i-1).subscribe()">
              {{ i }}
            </button>
          </div>
        </th>
      </tr>
      </tfoot>
    </table>
    <UserEditor v-if="editorOpened"
                v-model="activeUser"
                @submit="onUserSubmit"
                @cancel="editorOpened = false"/>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';

import userService from "@/services/UserService";
import {Observable} from "rxjs";
import {map, switchMap, tap} from "rxjs/operators";
import {UserView} from "@/administration/model/UserView";
import UserEditor from "@/administration/component/UserEditor.vue";
import {User} from "@/services/model/User";

@Options({
  name: 'UserAdminTab',
  components: {UserEditor},
})
export default class UserAdminTab extends Vue {
  private users: UserView[] = [];
  private pagesNumber = 0;
  private activePage = 0;
  private deleteEnable: boolean = this.checkState;
  private editorOpened: boolean = false;
  private activeUser: User = {} as User;

  mounted(): void {
    this.loadUserPage(0).subscribe();
  }

  get checkState(): boolean {
    const userView: UserView | undefined = this.users.find(f => f.isSelected);
    const isOneSelected = userView !== undefined;
    if (this.$refs && this.$refs['globalCheck'])
      this.$refs['globalCheck'].indeterminate = isOneSelected && this.users.find(f => !f.isSelected) !== undefined;
    return isOneSelected;
  }

  loadUserPage(page: number): Observable<UserView[]> {
    const resolvedPage = (page > 0) ? page : 0;
    return userService.list(resolvedPage).pipe(
        switchMap(page => {
          this.pagesNumber = page.totalPage;
          this.activePage = page.currentPage;
          return page.data;
        }),
        map(fs => fs.map(f => ({isSelected: false, data: f}))),
        tap(fs => this.users = fs)
    )
  }

  dateToString(date: string): string {
    return new Date(date).toLocaleDateString(navigator.languages, {
      timeZone: 'UTC',
      year: 'numeric', month: '2-digit', day: '2-digit', hour: "2-digit", minute: "2-digit"
    });
  }

  private onUserSubmit(event: Event): void {
    this.editorOpened = false;
    console.log('onUserSubmit: ', event);
  }
}
</script>
