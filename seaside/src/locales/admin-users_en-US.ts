const admin = await import('@/locales/admin_en-US');

export const en_US = {
    ...admin.en_US,
    'admin.users.add': 'add',
    'admin.users.import': 'import',
    'admin.users.export': 'export',
    'admin.users.delete': 'delete',
    'admin.users.login': 'login',
    'admin.users.username': 'username',
    'admin.users.mail': 'mail',
    'admin.users.role': 'role',
    'admin.users.createdAt': 'created at',
    'admin.users.lastActivity': 'last activity',
    'admin.users.messages.userIdCopied': 'User ID copied on clipboard !',
    'admin.users.messages.userCreatedSuccessfully': 'User {login} created successfully !',
    'admin.users.messages.userUpdatedSuccessfully': 'User {login} updated successfully !',
    'admin.users.messages.userDeletedSuccessfully': 'User {login} deleted successfully ! | All users deleted successfully !',
    'admin.users.messages.unableDeleteUser': 'Unable to delete user {login} ! | Unable to delete all selected users !',
    'admin.users.messages.configUsersDeletion': 'NOP ' +
        '| Delete user {login} ? ' +
        '| Delete all {count} selected users ?',
};