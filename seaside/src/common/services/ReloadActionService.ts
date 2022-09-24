class ReloadActionService {
    private reloadFunction: FunctionStringCallback = (context) => {
        console.warn(`no reload function for context ${context}!`)
    };

    /**
     * Register the function call on reload
     * This allows others components to reload news list
     *
     * @param apply [VoidFunction] The call function
     */
    registerReloadFunction(apply: FunctionStringCallback): void {
        this.reloadFunction = apply;
    }

    unregisterReloadFunction(): void {
        delete this.reloadFunction;
    }

    reload(context?: string): void {
        if (this.reloadFunction) {
            this.reloadFunction(context ? context : '');
        }
    }
}

export default new ReloadActionService();