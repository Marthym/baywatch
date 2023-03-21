import {User} from "@/teams/model/User.type";
import {MemberPending} from "@/teams/model/MemberPending.enum";

export type Team = {
    _id: string,
    _createdAt: string,
    _createdBy: string,
    _managers: User[],
    _me: { pending: MemberPending }
    name: string,
    topic: string,
}