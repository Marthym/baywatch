export type News = {
    id: string;
    title: string;
    description: string;
    publication: Date;
    image: string;
    link: string;
    feeds: string[];
    read: boolean;
    shared: boolean;
}
