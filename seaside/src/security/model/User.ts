import { email, forward, InferInput, minLength, object, partialCheck, pipe, string } from 'valibot';

export const UserAccountFormSchema = pipe(object({
    login: pipe(string(), minLength(3, 'security.register.message.login.too.short')),
    name: pipe(string(), minLength(3, 'security.register.message.name.too.short')),
    mail: pipe(string(), email('security.register.message.mail.invalid.format')),
    password: pipe(string(), minLength(8, 'security.register.message.password.too.short')),
    passwordConfirm: pipe(string(), minLength(8, 'security.register.message.confirm.too.short')),
}), forward(partialCheck(
        [['password'], ['passwordConfirm']],
        (input) => input.password === input.passwordConfirm,
        'security.register.message.confirm.different.password',
    ),
    ['passwordConfirm']));

export type UserAccountForm = InferInput<typeof UserAccountFormSchema>;

export type UserCreated = {
    login: string;
    name: string;
    mail: string;
    password: string;
    roles: string[];
}

type UserMeta = {
    _id: string;
    _createdAt: string;
    _loginAt?: string;
    _loginIP?: string;
}

export type User = UserMeta
    & Omit<UserCreated, 'password'> & { password?: string; };

export const ANONYMOUS: User = {
    _id: '0',
    _createdAt: new Date(0).toLocaleString(),
    login: '',
    name: 'Anonymous',
    mail: '',
    roles: [],
};