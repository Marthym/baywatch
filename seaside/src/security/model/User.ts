import {UserRole} from "@/security/model/UserRole.enum";

export type User = {
    _id?: string;
    _createdAt?: string;
    login: string;
    name: string;
    mail: string;
    role: UserRole;
    password?: string;
    confirm?: string;
}

export const ANONYMOUS: User = {
    login: '',
    name: 'Anonymous',
    mail: '',
    role: UserRole.ANONYMOUS,
};