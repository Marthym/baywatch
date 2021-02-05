import { from } from 'rxjs';

class NewsService {
    serviceBaseUrl: String = process.env.BASE_URL;

    getNews(): any {
        from(fetch(this.serviceBaseUrl+"/"))
    }
}