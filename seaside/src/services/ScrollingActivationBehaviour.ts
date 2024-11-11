import ScrollActivable from '@/services/model/ScrollActivable';
import { Vue } from 'vue-facing-decorator';

/**
 * Allow a List Vue Component to activate Element on scroll without scroll event.
 *
 * The {ScrollingActivationBehaviour} use an {IntersectionObserver} to detect an Element leaving the
 * visible.
 *
 */
export class ScrollingActivationBehaviour {
    private observer: IntersectionObserver | null = null;

    public connect(component: ScrollActivable & typeof Vue): void {
        this.observer = new IntersectionObserver((entries) => {
            const entry = entries[0];
            if (!entry.isIntersecting && entry.rootBounds !== undefined && entry.rootBounds !== null) {
                const isAbove = entry.boundingClientRect.y < entry.rootBounds.y;
                const incr = (isAbove) ? +1 : -1;
                component.onScrollActivation(incr);
            }
        }, { threshold: [0.25], rootMargin: '-60px 0px 0px 0px' });
    }

    public disconnect(): void {
        if (this.observer) {
            this.observer.disconnect();
        }
    }

    public observe(el: Element): void {
        this.observer?.disconnect();
        this.observer?.observe(el);
    }
}

export function useScrollingActivation() {
    return new ScrollingActivationBehaviour();
}