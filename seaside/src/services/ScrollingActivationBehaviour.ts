import ScrollActivable from "@/services/model/ScrollActivable";
import Vue from "vue";

/**
 * Allow a List Vue Component to activate Element on scroll without scroll event.
 *
 * The {ScrollingActivationBehaviour} use an {IntersectionObserver} to detect an Element leaving the
 * visible.
 *
 */
export default class ScrollingActivationBehaviour {
    private readonly observer: IntersectionObserver;

    constructor(component: ScrollActivable & Vue) {
        this.observer = new IntersectionObserver((entries) => {
            const entry = entries[0];
            if (!entry.isIntersecting && entry.rootBounds !== null) {
                const isAbove = entry.boundingClientRect.y < entry.rootBounds.y;
                const incr = (isAbove) ? +1 : -1;
                component.activateElement(incr);
            }
        }, {threshold: [0.75], rootMargin: "-60px 0px 0px 0px"});
    }

    public static apply(component: ScrollActivable & Vue): ScrollingActivationBehaviour {
        const decorator = new ScrollingActivationBehaviour(component);
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