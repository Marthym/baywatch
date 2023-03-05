import {MemberPending} from "@/teams/model/MemberPending.enum";

export type Member = {
    _id: string,
    _user: {
        _id: string,
        name: string,
        roles: string[],
    },
    pending: MemberPending,
}