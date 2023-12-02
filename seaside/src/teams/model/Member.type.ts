import {MemberPending} from "@/teams/model/MemberPending.enum";
import {User} from "@/teams/model/User.type";

export type Member = {
    _id: string,
    _user: User,
    pending: MemberPending,
}