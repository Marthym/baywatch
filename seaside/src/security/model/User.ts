export type User = {
    _id?: string;
    _createdAt?: string;
    _loginAt?: string;
    _loginIP?: string;
    login: string;
    name: string;
    mail: string;
    roles: string[];
    password?: string;
}

export const ANONYMOUS: User = {
    login: '',
    name: 'Anonymous',
    mail: '',
    roles: [],
};