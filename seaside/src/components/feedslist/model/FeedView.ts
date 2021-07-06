import {Feed} from "@/services/model/Feed";

export type FeedView = {
    icon: string;
    data: Feed;
    isSelected: boolean;
}