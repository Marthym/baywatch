import {Member} from "@/teams/model/Member.type";

export type Team = {
    _id: string,
    _createdAt: string,
    _createdBy: string,
    _managers: Member[],
    name: string,
    topic: string,
}