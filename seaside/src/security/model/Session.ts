import {User} from "@/security/model/User";

export type Session = {
    user: User;
    maxAge: number;
}