import {News} from "@/techwatch/model/News";

export type NewsView = {
    data: News;
    feeds: string[];
    isActive: boolean;

    /**
     * True if the user has explicitly mark the card.
     * The card does not change mark when change active
     */
    keepMark: boolean;
}