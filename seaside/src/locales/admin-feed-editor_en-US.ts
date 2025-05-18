import { en_US as admin_en_US } from '@/locales/admin_en-US';
import { backend_scraping_en_US } from '@/locales/backend/scraping_en-US';

export const en_US = {
    ...admin_en_US,
    ...backend_scraping_en_US,
    'admin.feed.editor.description': 'Description',
    'admin.feed.editor.description.placeholder': 'Enter a short description',
    'admin.feed.editor.errors': 'Errors',
    'admin.feed.editor.errors.last': 'Last :',
    'admin.feed.editor.errors.since': 'Since :',
    'admin.feed.editor.icon': 'Feed icon',
    'admin.feed.editor.icon.placeholder': 'Feed icon URL: https://...',
    'admin.feed.editor.location': 'Location',
    'admin.feed.editor.location.placeholder': 'Feed URL: https://...',
    'admin.feed.editor.name': 'Feed Name',
    'admin.feed.editor.name.placeholder': 'Enter the feed name',
    'admin.feed.editor.title': 'Update feed',
    'admin.feeds.messages.feedUpdateFailed': 'Fail to update the feed!',
    'admin.feeds.messages.feedUpdatedSuccessfully': 'Feed updated successfully!',
};