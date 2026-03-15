import { email, InferInput, minLength, object, pipe, string } from 'valibot';

export const UserAccountFormSchema = pipe(object({
    login: pipe(string(), minLength(3, 'security.register.message.login.too.short')),
    name: pipe(string(), minLength(3, 'security.register.message.name.too.short')),
    mail: pipe(string(), email('security.register.message.mail.invalid.format')),
}));

export type UserAccountForm = InferInput<typeof UserAccountFormSchema>;

export type UserCreated = {
    login: string;
    name: string;
    mail: string;
    password?: string;
    roles: string[];
}

type UserMeta = {
    _id: string;
    _createdAt: string;
    _loginAt?: string;
    _loginIP?: string;
}

export type User = UserMeta & UserCreated;

export const ANONYMOUS: User = {
    _id: '0',
    _createdAt: new Date(0).toLocaleString(),
    login: '',
    name: '',
    mail: '',
    roles: [],
};