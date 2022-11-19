import {News} from "@/techwatch/model/News.type";

export type NewsView = {
    data: News;
    isActive: boolean;

    sizes: string;
    srcset: string;

    /**
     * True if the user has explicitly mark the card.
     * The card does not change mark when change active
     */
    keepMark: boolean;
}