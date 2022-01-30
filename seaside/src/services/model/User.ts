import {UserRole} from "@/services/model/UserRole.enum";

export type User = {
    id: string;
    login: string;
    name: string;
    mail: string;
    role: UserRole;
}

export const ANONYMOUS: User = {
    id: '',
    login: '',
    name: 'Anonymous',
    mail: '',
    role: UserRole.ANONYMOUS,
};