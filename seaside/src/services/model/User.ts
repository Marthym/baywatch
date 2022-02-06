import {UserRole} from "@/services/model/UserRole.enum";

export type User = {
    _id: string;
    _createdAt: string;
    login: string;
    name: string;
    mail: string;
    role: UserRole;
}

export const ANONYMOUS: User = {
    _id: '',
    _createdAt: '',
    login: '',
    name: 'Anonymous',
    mail: '',
    role: UserRole.ANONYMOUS,
};