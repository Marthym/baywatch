import {
    array,
    email,
    forward,
    InferInput,
    minLength,
    nonEmpty,
    object,
    optional,
    partialCheck,
    pipe,
    string,
} from 'valibot';

export const UserAccountFormSchema = pipe(object({
    login: pipe(string(), minLength(3, 'admin.users.editor.message.login.too.short')),
    name: pipe(string(), minLength(3, 'admin.users.editor.message.name.too.short')),
    mail: pipe(string(), email('admin.users.editor.message.mail.invalid.format')),
    password: optional(pipe(string(), minLength(8, 'admin.users.editor.message.password.too.short'))),
    passwordConfirm: optional(pipe(string(), minLength(8, 'admin.users.editor.message.confirm.too.short'))),
    roles: pipe(array(string()), nonEmpty('admin.users.editor.message.role_incorrect')),
}), forward(
    partialCheck([['password'], ['passwordConfirm']],
        (input) => input.password === input.passwordConfirm,
        'admin.users.editor.message.confirm.different.password',
    ), ['passwordConfirm']));

export type UserAccountForm = InferInput<typeof UserAccountFormSchema>;
