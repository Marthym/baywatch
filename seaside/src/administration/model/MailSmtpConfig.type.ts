import {
    boolean,
    email,
    InferInput,
    minLength,
    minValue,
    nonEmpty,
    number,
    object,
    optional,
    pipe,
    string,
    trim,
} from 'valibot';

export const MailSmtpConfigSchema = object({
    host: pipe(string()),
    port: pipe(number(), minValue(1)),
    secure: boolean(),
    username: pipe(string(), minLength(3)),
    password: optional(pipe(string(), minLength(3))),
    requireTls: boolean(),
    from: pipe(string(), email()),
    ssl: object({
        protocols: pipe(string(), trim(), nonEmpty()),
        checkserveridentity: boolean(),
    }),
    pollingIntervalSeconds: pipe(number(), minValue(2)),
});

export type MailSmtpConfig = InferInput<typeof MailSmtpConfigSchema>;
