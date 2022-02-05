<template>
  <div class="overflow-x-auto mt-4">
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
        <td>12/16/2020</td>
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
  </div>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-property-decorator';

import userService from "@/services/UserService";
import {Observable} from "rxjs";
import {map, switchMap, tap} from "rxjs/operators";
import {UserView} from "@/administration/model/UserView";

@Options({
  name: 'UserAdminTab',
  components: {},
})
export default class UserAdminTab extends Vue {
  private users: UserView[] = [];
  private pagesNumber = 0;
  private activePage = 0;

  mounted(): void {
    this.loadUserPage(0).subscribe();
  }

  get checkState(): boolean {
    const isOneSelected = this.users.find(f => f.isSelected) !== undefined;
    if (this.$refs['globalCheck'])
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
}
</script>
