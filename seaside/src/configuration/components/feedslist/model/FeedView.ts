import {Feed} from "@/configuration/model/Feed.type";

export type FeedView = {
    icon: string;
    data: Feed;
    isSelected: boolean;
}