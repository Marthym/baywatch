class News {
    public id: string;
    public title: string;
    public description: string;
    public publication: Date;
    public image: string;
    public link: string;


    constructor(id: string, title: string, description: string, publication: Date, image: string, link: string) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.publication = publication;
        this.image = image;
        this.link = link;
    }
}