DROP SCHEMA if exists dbo CASCADE;
create schema dbo;

drop table if exists dbo.Users;
drop table if exists dbo.RefreshTokens;

create table dbo.Users
(
    id                  serial primary key,
    name                VARCHAR(64)         not null,
    username            VARCHAR(64) unique  not null,
    hashed_password     VARCHAR(128)        not null,
    email               VARCHAR(128) unique not null,
    description         VARCHAR(512),
    profile_picture     BYTEA,
    profile_picture_type VARCHAR(128)
);

create table dbo.RefreshTokens
(
    token_validation VARCHAR(512) primary key,
    user_id          int references dbo.Users (id),
    expiration_date  TIMESTAMP NOT NULL,

    UNIQUE (user_id, token_validation)
);