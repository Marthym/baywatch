export type MailSmtpConfig = {
    host: string;
    port: number;
    secure: boolean;
    username: string;
    password: string;
    sslProtocols: string;
    requireTls: boolean;
    sslCheckserveridentity: boolean;
    from: string;
    pollingIntervalSeconds: number;
};
