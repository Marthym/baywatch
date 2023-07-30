<template>
  <ModalWindow title="Change password" :is-visible="isOpen">
    <input v-model="oldPassword" type="text" placeholder="Old Password" class="input input-bordered w-full mt-4"/>
    <input v-model="newPassword" type="text" placeholder="New Password" class="input input-bordered w-full my-4"/>
    <input v-model="confirmPassword" type="text" placeholder="Confirm Password" class="input input-bordered w-full"/>
    <template v-slot:actions>
      <button class="btn btn-primary"
              @click.stop="$emit('submit', {old: oldPassword, new: confirmPassword})">
        Fermer
      </button>
      <button class="btn" @click.stop="$emit('cancel')">Fermer</button>
    </template>
  </ModalWindow>
</template>
<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator';
import ModalWindow from '@/common/components/ModalWindow.vue';

@Component({
  name: 'ChangePasswordModal',
  components: { ModalWindow },
  emits: ['submit', 'cancel'],
})
export default class ChangePasswordModal extends Vue {
  @Prop({ default: false }) private isOpen: boolean = false;
  private oldPassword: string;
  private newPassword: string;
  private confirmPassword: string;
}
</script>
