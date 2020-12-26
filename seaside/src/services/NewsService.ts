class NewsService {
    serviceBaseUrl: String = process.env.BASE_URL;

    getNews(): Array<News> {
        fetch(this.serviceBaseUrl+"/")
    }
}