DROP SCHEMA if exists dbo CASCADE;
create schema dbo;

CREATE TYPE dbo.VisibilityType AS ENUM ('Public', 'Private');

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

CREATE TABLE dbo.Event (
    id          serial primary key,
    title       VARCHAR(64) NOT NULL,
    description VARCHAR(512),
    category    VARCHAR(64) NOT NULL,
    subcategory VARCHAR(64),
    location    VARCHAR(255) NOT NULL,
    visibility  dbo.VisibilityType NOT NULL,
    date        TIMESTAMP,
    end_date    TIMESTAMP,
    priceAmount DECIMAL(10, 2),
    priceCurrency VARCHAR(3),
    password    VARCHAR(64)
);

CREATE TABLE dbo.Task (
    id          serial primary key,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(512),
    event_id    INT NOT NULL,
    user_id     INT NOT NULL,
    FOREIGN KEY (event_id) REFERENCES dbo.Event(id),
    FOREIGN KEY (user_id) REFERENCES dbo.Users(id)
);

CREATE TABLE dbo.UserParticipatesInEvent (
    user_id  INT not null,
    event_id INT not null,
    PRIMARY KEY (user_id, event_id),
    FOREIGN KEY (user_id) REFERENCES dbo.Users(id),
    FOREIGN KEY (event_id) REFERENCES dbo.Event(id)
);

