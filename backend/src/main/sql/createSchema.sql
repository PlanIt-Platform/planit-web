DROP SCHEMA if exists dbo CASCADE;
create schema dbo;

drop table if exists dbo.Users;
drop table if exists dbo.RefreshTokens;

create table dbo.Users
(
    id                  serial primary key,
    username            VARCHAR(64) unique  not null,
    hashed_password VARCHAR(256)        not null,
    email               VARCHAR(320) unique not null
);

create table dbo.RefreshTokens
(
    token_validation VARCHAR(512) primary key,
    user_id          int references dbo.Users (id),
    expiration_date  TIMESTAMP NOT NULL,

    UNIQUE (user_id, token_validation)
);