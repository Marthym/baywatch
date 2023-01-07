export type User = {
    _id?: string;
    _createdAt?: string;
    login: string;
    name: string;
    mail: string;
    roles: string[];
    password?: string;
    confirm?: string;
}

export const ANONYMOUS: User = {
    login: '',
    name: 'Anonymous',
    mail: '',
    roles: [],
};