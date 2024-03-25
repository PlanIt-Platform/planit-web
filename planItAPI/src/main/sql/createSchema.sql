DROP SCHEMA if exists dbo CASCADE;
create schema dbo;

drop table if exists dbo.Users;

create table dbo.Users
(
    id                  serial primary key,
    username            VARCHAR(64) unique  not null,
    hashed_password VARCHAR(256)        not null,
    email               VARCHAR(320) unique not null
);