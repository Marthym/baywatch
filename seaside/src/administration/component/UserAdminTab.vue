<template>
  <div class="overflow-x-auto mt-4">
    <div class="md:btn-group mb-2">
      <button class="btn btn-sm btn-ghost mb-2 mr-2 md:m-0" @click.prevent="onUserAdd()">
        <svg class="w-6 h-6 md:mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
             xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
        <span>Ajouter</span>
      </button>
      <button class="btn btn-sm mb-2 mr-2 md:m-0" @click="">
        <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
             xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
        </svg>
        Importer
      </button>
      <a class="btn btn-sm mb-2 mr-2 md:m-0">
        <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
             xmlns="http://www.w3.org/2000/svg">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path>
        </svg>
        Exporter
      </a>
      <button class="btn btn-sm btn-error mb-2 mr-2 md:m-0" :disabled="!checkState" @click="onUserBulkDelete()">
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
    <table class="table w-full table-compact" aria-describedby="User List">
      <thead>
      <tr>
        <th scope="col">
          <label>
            <input type="checkbox" class="checkbox" ref="globalCheck"
                   :checked="checkState" @change="onSelectAll()"/>
            <span class="checkbox-mark"></span>
          </label>
        </th>
        <th scope="col">Pseudo</th>
        <th scope="col">Nom</th>
        <th scope="col">Mail</th>
        <th scope="col">Role</th>
        <th scope="col">Created At
          <div class="btn-group justify-end" v-if="pagesNumber > 1">
            <button v-for="i in pagesNumber" :key="i"
                    :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
                    v-on:click="loadUserPage(i-1).subscribe()">
              {{ i }}
            </button>
          </div>
        </th>
        <th scope="col">&nbsp;</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="vUser in this.users" v-bind:key="vUser.data._id">
        <th scope="row">
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
        <td>
          <div class="btn-group justify-end">
            <button class="btn btn-sm btn-square btn-ghost" @click.prevent="onUserEdit(vUser.data)">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
                   stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"/>
              </svg>
            </button>
            <button class="btn btn-sm btn-square btn-ghost" @click.prevent="onUserDelete(vUser.data)">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
                   stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
              </svg>
            </button>
          </div>
        </td>
      </tr>
      </tbody>
      <tfoot>
      <tr>
        <th scope="col"></th>
        <th scope="col">Pseudo</th>
        <th scope="col">Nom</th>
        <th scope="col">Mail</th>
        <th scope="col">Role</th>
        <th scope="col">Created At
          <div class="btn-group justify-end" v-if="pagesNumber > 1">
            <button v-for="i in pagesNumber" :key="i"
                    :class="{'btn-active': activePage === i-1}" class="btn btn-sm"
                    v-on:click="loadUserPage(i-1).subscribe()">
              {{ i }}
            </button>
          </div>
        </th>
        <th scope="col">&nbsp;</th>
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

import notificationService from "@/services/notification/NotificationService";
import userService from "@/security/services/UserService";
import {Observable} from "rxjs";
import {filter, map, switchMap, tap} from "rxjs/operators";
import {UserView} from "@/administration/model/UserView";
import UserEditor from "@/administration/component/UserEditor.vue";
import {User} from "@/security/model/User";
import {AlertResponse, AlertType} from "@/common/components/alertdialog/AlertDialog.types";

@Options({
  name: 'UserAdminTab',
  components: {UserEditor},
})
export default class UserAdminTab extends Vue {
  private users: UserView[] = [];
  private pagesNumber = 0;
  private activePage = 0;
  private editorOpened: boolean = false;
  private activeUser: User;

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

  private onSelectAll(): void {
    const current = this.checkState;
    this.users.forEach(f => f.isSelected = !current);
  }

  private onUserSubmit(): void {
    const edit = '_id' in this.activeUser && this.activeUser._id !== undefined;
    if (edit) {
      userService.update(this.activeUser).subscribe({
        next: user => {
          notificationService.pushSimpleOk(`User ${user.login} updated successfully !`);
          this.editorOpened = false;
        },
        error: e => notificationService.pushSimpleError(e.message),
      });
    } else {
      userService.add(this.activeUser).subscribe({
        next: user => {
          this.users.push({isSelected: false, data: user});
          notificationService.pushSimpleOk(`User ${user.login} created successfully !`);
          this.editorOpened = false;
        },
        error: e => notificationService.pushSimpleError(e.message),
      });
    }
  }

  private onUserAdd(): void {
    this.activeUser = {} as User;
    this.editorOpened = true;
  }

  private onUserEdit(user: User): void {
    this.activeUser = user;
    this.editorOpened = true;
  }

  private onUserDelete(user: User): void {
    const message = `Supprimer l’utilisateur <br/> <b>${user.name}</b>`;
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => userService.remove(user._id as string)),
        tap(() => {
          const idx = this.users.findIndex(fv => fv.data._id === user._id);
          this.users.splice(idx, 1);
        })
    ).subscribe({
      next: user => notificationService.pushSimpleOk(`User ${user.name} deleted successfully !`),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError(`Unable to delete user ${user.name} !`);
      }
    });
  }

  private onUserBulkDelete(): void {
    const ids = this.users.filter(f => f.isSelected);
    if (ids.length == 0) {
      return;
    } else if (ids.length == 1) {
      return this.onUserDelete(ids[0].data);
    }
    const message = `Delete all ${ids.length} selected users ?`;
    this.$alert.fire(message, AlertType.CONFIRM_DELETE).pipe(
        filter(response => response === AlertResponse.CONFIRM),
        switchMap(() => userService.bulkRemove(ids.map(uv => uv.data._id) as string[])),
        tap(() => {
          ids.forEach(id => {
            const idx = this.users.findIndex(uv => uv.data._id === id.data._id);
            this.users.splice(idx, 1);
          })
        })
    ).subscribe({
      next: () => notificationService.pushSimpleOk('All users deleted successfully !'),
      error: e => {
        console.error(e);
        notificationService.pushSimpleError('Unable to delete all selected users !');
      }
    });
  }
}
</script>
