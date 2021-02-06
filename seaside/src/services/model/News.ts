class News {
    public id: string;
    public title: string;
    public description: string;
    public publication: Date;
    public link: string;


    constructor(id: string, title: string, description: string, publication: Date, link: string) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.publication = publication;
        this.link = link;
    }
}