export const UNAUTHORIZED = 'UNAUTHORIZED';
export const INVALID_SYNTAX = 'InvalidSyntax';

export type GqlErrorClassification = typeof UNAUTHORIZED | typeof INVALID_SYNTAX

export type GraphqlData = unknown

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

export type GraphqlResponse = {
    data: GraphqlData,
    errors: GraphqlErrors
}
