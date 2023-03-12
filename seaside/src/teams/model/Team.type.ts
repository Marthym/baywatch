import {User} from "@/teams/model/User.type";

export type Team = {
    _id: string,
    _createdAt: string,
    _createdBy: string,
    _managers: User[],
    name: string,
    topic: string,
}