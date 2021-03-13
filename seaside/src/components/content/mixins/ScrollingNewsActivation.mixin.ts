import Component from "vue-class-component";
import Vue from "vue";

@Component
export default class ScrollingNewsActivationMixin extends Vue {
    observer = new IntersectionObserver((entries) => {
        const entry = entries[0];
        if (!entry.isIntersecting && entry.rootBounds !== null) {
            const isAbove = entry.boundingClientRect.y < entry.rootBounds.y;
            const nextActive = (isAbove) ? this.getActiveNews() + 1 : this.getActiveNews() - 1;
            console.log("intersection: ", this.getActiveNews(), isAbove);
            this.activateNewsCard(nextActive);
        }
    }, {threshold: [1], rootMargin: "-50px 0px 0px 0px"});

    getActiveNews(): number {
        return 0;
    }

    activateNewsCard(idx: number): void {

    }
}