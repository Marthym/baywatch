import { en_US as admin_en_US } from '@/locales/admin_en-US';

export const en_US = {
    ...admin_en_US,
    'admin.feeds.confirm.feedsDeletion': 'Permanently delete the feed "{feed}" from the database ?' +
        '| Delete the {n} selected feeds from the database ?',
    'admin.feeds.info.deletionMustContainsOneID': 'You must select at least one feed to delete!',
    'admin.feeds.messages.feedDeletedSuccessfully': 'Feed {feed} successfully deleted!' +
        '| {n} feeds successfully deleted!',
    'admin.feeds.messages.feedDeletionFailed': 'An error occurred while deleting the feed(s)!',
    'admin.feeds.tab.feeds.list': 'Feed list',
};