const config = await import('@/locales/config_en-US');
export const en_US = {
    ...config.en_US,
    'config.settings.form.preferredLocale': 'Choose your preferred language',
    'config.settings.form.action.save': 'Save',
    'config.settings.messages.settingsUpdateSuccessfully': 'Settings updated successfully',
    'config.settings.messages.unableToUpdate': 'Unable to update settings',
};
