const EMPTY_CALLBACK: FunctionStringCallback = (context) => {
    console.warn(`no reload function for context ${context}!`);
};
let reloadFunction: FunctionStringCallback = EMPTY_CALLBACK;

export function actionServiceReload(context?: string): void {
    if (reloadFunction) {
        reloadFunction(context ? context : '');
    }
}

class ReloadActionService {

    /**
     * Register the function call on reload
     * This allows others components to reload news list
     *
     * @param apply [VoidFunction] The call function
     */
    registerReloadFunction(apply: FunctionStringCallback): void {
        reloadFunction = apply;
    }

    unregisterReloadFunction(): void {
        reloadFunction = EMPTY_CALLBACK;
    }

    reload(context?: string): void {
        actionServiceReload(context);
    }
}

export default new ReloadActionService();