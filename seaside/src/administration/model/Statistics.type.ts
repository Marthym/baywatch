import {Counter} from "@/administration/model/Counter.type";

export type Statistics = {
    news: Counter;
    feeds: Counter;
    users: Counter;
}