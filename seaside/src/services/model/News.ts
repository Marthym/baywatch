class News {
    id: String;
    public title: String;
    public description: String;
    public publication: Date;
    public link: String;


    constructor(id: String, title: String, description: String, publication: Date, link: String) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.publication = publication;
        this.link = link;
    }
}