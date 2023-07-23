const EMPTY_CALLBACK: FunctionStringCallback = (context) => {
    console.warn(`no reload function for context ${context}!`);
};
let reloadFunction: FunctionStringCallback = EMPTY_CALLBACK;

export function actionServiceReload(context?: string): void {
    if (reloadFunction) {
        reloadFunction(context ? context : '');
    }
}

export function actionServiceUnregisterFunction(): void {
    reloadFunction = EMPTY_CALLBACK;
}

/**
 * Register the function call on reload
 * This allows others components to reload news list
 *
 * @param apply [VoidFunction] The call function
 */
export function actionServiceRegisterFunction(apply: FunctionStringCallback): void {
    reloadFunction = apply;
}
