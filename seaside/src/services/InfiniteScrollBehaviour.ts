import Vue from "vue";
import InfiniteScrollable from "@/services/model/InfiniteScrollable";
import {take, takeLast} from "rxjs/operators";

export default class InfiniteScrollBehaviour {
    private readonly observer: IntersectionObserver;

    constructor(component: InfiniteScrollable & Vue) {
        this.observer = new IntersectionObserver((entries) => {
            const entry = entries[0];
            if (entry.isIntersecting) {
                this.observer.disconnect();
                component.loadNextPage().pipe(
                    takeLast(3),
                    take(1)
                ).subscribe(el => {
                    console.log("elements", el);
                    this.observer.observe(el);
                });
            }
        }, {threshold: [0], rootMargin: "-50px 0px 0px 0px"});
    }

    public static apply(component: InfiniteScrollable & Vue): InfiniteScrollBehaviour {
        const decorator = new InfiniteScrollBehaviour(component);
        component.$once('hook:beforeDestroy', () => {
            decorator.observer.disconnect();
        });
        return decorator;
    }

    public observe(el: Element): void {
        this.observer.disconnect();
        this.observer.observe(el);
    }
}