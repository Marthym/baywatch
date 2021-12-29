import Vue from "vue";
import InfiniteScrollable from "@/services/model/InfiniteScrollable";
import {take, takeLast} from "rxjs/operators";

export class InfiniteScrollBehaviour {
    private observer: IntersectionObserver;

    public connect(component: InfiniteScrollable & Vue): void {
        this.observer = new IntersectionObserver((entries) => {
            const entry = entries[0];
            if (entry.isIntersecting) {
                this.observer.disconnect();
                component.loadNextPage().pipe(
                    takeLast(3),
                    take(1)
                ).subscribe(el => {
                    this.observer.observe(el);
                });
            }
        }, {threshold: [0], rootMargin: "-50px 0px 0px 0px"});
    }

    public observe(el: Element): void {
        this.observer.disconnect();
        this.observer.observe(el);
    }

    public disconnect() {
        this.observer.disconnect();
    }
}

export function useInfiniteScroll() {
    return new InfiniteScrollBehaviour();
}