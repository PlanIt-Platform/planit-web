DROP SCHEMA if exists dbo CASCADE;
create schema dbo;

drop table if exists dbo.Task;
drop table if exists dbo.Message;
drop table if exists dbo.Chat;
drop table if exists dbo.UserParticipatesInEvent;
drop table if exists dbo.UserParticipatesInEventChat;
drop table if exists dbo.RefreshTokens;
drop table if exists dbo.Users;
drop table if exists dbo.Event;
drop type if exists dbo.VisibilityType;
drop type if exists dbo.LocationType;

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

CREATE TYPE dbo.VisibilityType AS ENUM ('Public', 'Private');
CREATE TYPE dbo.LocationType AS ENUM ('Online', 'Physical');

CREATE TABLE dbo.Event (
    id          serial primary key,
    title       VARCHAR(64) NOT NULL,
    description VARCHAR(512),
    category    VARCHAR(64) NOT NULL,
    locationType dbo.LocationType,
    location    VARCHAR(255),
    latitude    DECIMAL(8, 6),
    longitude   DECIMAL(9, 6),
    visibility  dbo.VisibilityType NOT NULL,
    date        TIMESTAMP,
    end_date    TIMESTAMP,
    priceAmount DECIMAL(10, 2),
    priceCurrency VARCHAR(3),
    password    VARCHAR(64),
    code        VARCHAR(6)
);

CREATE TABLE dbo.UserParticipatesInEvent (
  user_id  INT not null,
  event_id INT not null,
  PRIMARY KEY (user_id, event_id),
  FOREIGN KEY (user_id) REFERENCES dbo.Users(id),
  FOREIGN KEY (event_id) REFERENCES dbo.Event(id)
);

CREATE TABLE dbo.Polls (
    id serial PRIMARY KEY,
    title VARCHAR(255),
    duration INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    organizer_id INT,
    event_id INT,
    FOREIGN KEY (organizer_id) REFERENCES dbo.Users(id),
    FOREIGN KEY (event_id) REFERENCES dbo.Event(id)
);

CREATE TABLE dbo.Options (
    id serial PRIMARY KEY,
    text VARCHAR(255),
    poll_id INT,
    FOREIGN KEY (poll_id) REFERENCES dbo.Polls(id)
);

CREATE TABLE dbo.UserVotes (
   user_id INT,
   option_id INT,
   poll_id INT,
   PRIMARY KEY (user_id, option_id, poll_id),
   FOREIGN KEY (user_id) REFERENCES dbo.Users(id),
   FOREIGN KEY (option_id) REFERENCES dbo.Options(id),
    FOREIGN KEY (poll_id) REFERENCES dbo.Polls(id)
);

CREATE TABLE dbo.Roles (
    id serial PRIMARY KEY,
    name VARCHAR(255) not null,
    event_id INT,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES dbo.Users(id),
    FOREIGN KEY (event_id) REFERENCES dbo.Event(id),
    CONSTRAINT check_role_name CHECK (name IN ('Participant', 'Organizer'))
);

CREATE TABLE dbo.Feedback (
    id serial PRIMARY KEY,
    text VARCHAR(255),
    date TIMESTAMP
);

insert into dbo.Roles (name) values ('Participant');
insert into dbo.Roles (name) values ('Organizer');