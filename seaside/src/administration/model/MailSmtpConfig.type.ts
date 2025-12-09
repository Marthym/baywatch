export type MailSmtpConfig = {
    host: string;
    port: number;
    secure: boolean;
    username: string;
    password: string;
    requireTls: boolean;
    ssl: {
        protocols: string;
        checkserveridentity: boolean
    };
    from: string;
    pollingIntervalSeconds: number;
};
