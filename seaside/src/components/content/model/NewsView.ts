import {News} from "@/services/model/News";

export type NewsView = {
    data: News;
    isActive: boolean;
    isRead: boolean;
}