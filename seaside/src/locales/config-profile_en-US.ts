import { smarttable_en_US } from '@/locales/smarttable_en-US';

const config = await import('@/locales/config_en-US');
export const en_US = {
    ...smarttable_en_US,
    ...config.en_US,
    'config.profile.form.id': 'id',
    'config.profile.form.login': 'login',
    'config.profile.form.name': 'name',
    'config.profile.form.mail': 'mail address',
    'config.profile.form.password': 'change password',
    'config.profile.form.password.placeholder': 'Enter your password to update your profile',
    'config.profile.form.password.tooltip': 'Click to change your password',
    'config.profile.form.action.save': 'update profile',
    'config.profile.avatar.notice': 'the Avatar was grab from gravatar depending on your mail address',
    'config.profile.mail.notice': 'mail address is only use for avatar and security transactions',
    'config.profile.messages.unableToUpdate': 'Unable to update user !',
    'config.profile.messages.profileUpdateSuccessfully': 'Profile updated successfully !',
    'config.password.title': 'change password',
    'config.password.form.old.placeholder': 'old password',
    'config.password.form.new.placeholder': 'new password',
    'config.password.form.confirm.placeholder': 'confirm password',
    'config.password.form.action.submit': 'update',
    'config.password.error.old.mandatory': 'You must enter old password',
    'config.password.error.new.unsecure': 'This password is not secure. An attacker will find it instant !',
    'config.password.error.confirm.different': 'The new and confirmation passwords must be the same',
};
