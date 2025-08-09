create table MAIL_TEMPLATES
(
    MATE_ID      VARCHAR(28)  not null primary key,
    MATE_NAME    VARCHAR(250) not null,
    MATE_LANG    VARCHAR(5)   not null,
    MATE_SUBJECT VARCHAR(250) not null,
    MATE_BODY    TEXT         not null
);

create unique index IDX_MAIL_TEMPLATES_NAME_LANG
    on MAIL_TEMPLATES (MATE_NAME, MATE_LANG);

insert into MAIL_TEMPLATES
values ('TP01K24EWVJ5N9GRFYHCHRN9J4PM', 'PASSWORD_RESET', 'en-US',
        'Baywatch - Request for password reset',
        '<p>Hello you ask for password reset.</p>' ||
        '<a href="${rootUrl}/password/reset?token${token}">Click here</a> to reset your password.'),
       ('TP01K24EWVZ9S7QM3GK5JPGY0J3F', 'PASSWORD_RESET', 'fr-FR',
        'Baywatch - Réinitialisation de mot de passe',
        '<p>Bonjour vous avez demandé la réinitialisation de votre mot de passe.</p>' ||
        '<a href="${rootUrl}/password/reset?token${token}">Click here</a> to reset your password.');
