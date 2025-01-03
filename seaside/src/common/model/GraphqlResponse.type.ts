export const BAD_REQUEST = 'BAD_REQUEST';
export const FORBIDDEN = 'FORBIDDEN';
export const INTERNAL_ERROR = 'INTERNAL_ERROR';
export const INVALID_SYNTAX = 'InvalidSyntax';
export const UNAUTHORIZED = 'UNAUTHORIZED';
export const VALIDATION_ERROR = 'ValidationError';

export type GqlErrorClassification =
    typeof BAD_REQUEST |
    typeof FORBIDDEN |
    typeof INTERNAL_ERROR |
    typeof INVALID_SYNTAX |
    typeof UNAUTHORIZED |
    typeof VALIDATION_ERROR;

export type GraphqlErrors = {
    message: string;            // Required for all errors
    locations?: {
        line: number
        column: number
    }[];
    path?: string[],
    extensions: {
        classification: GqlErrorClassification
        translation?: string
        properties?: string[]
    },
    [propName: string]: any;    // 7.2.2 says 'GraphQL servers may provide additional entries to error'
}[]

export type GraphqlResponse<T> = {
    data: T,
    errors: GraphqlErrors
}
