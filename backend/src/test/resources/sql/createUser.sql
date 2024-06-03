DROP SCHEMA if exists dbo CASCADE;
create schema dbo;

create table dbo.Users
(
    id                  serial primary key,
    name                VARCHAR(64)         not null,
    username            VARCHAR(64) unique  not null,
    hashed_password     VARCHAR(128)        not null,
    email               VARCHAR(128) unique not null,
    description         VARCHAR(512),
    interests           VARCHAR(512)
);

create table dbo.RefreshTokens
(
    token_validation VARCHAR(512) primary key,
    user_id          int references dbo.Users (id),
    expiration_date  TIMESTAMP NOT NULL,

    UNIQUE (user_id, token_validation)
);