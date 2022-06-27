export const UNAUTHORIZED = 'UNAUTHORIZED';
export const INVALID_SYNTAX = 'InvalidSyntax';
export const INTERNAL_ERROR = 'INTERNAL_ERROR';

export type GqlErrorClassification = typeof UNAUTHORIZED | typeof INVALID_SYNTAX | typeof INTERNAL_ERROR;

export type GraphqlErrors = {
    message: string;            // Required for all errors
    locations?: {
        line: number,
        column: number
    }[];
    path?: string[],
    extension: {
        classification: GqlErrorClassification
    }[],
    [propName: string]: any;    // 7.2.2 says 'GraphQL servers may provide additional entries to error'
}[]

export type GraphqlResponse<T> = {
    data: T,
    errors: GraphqlErrors
}
