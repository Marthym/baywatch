import {User} from "@/services/model/User";

export type Session = {
    user: User;
    maxAge: number;
}