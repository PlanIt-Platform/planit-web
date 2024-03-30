DROP SCHEMA if exists dbo CASCADE;
create schema dbo;

drop table if exists dbo.Users;
drop table if exists dbo.RefreshTokens;
drop table if exists dbo.Event;
drop table if exists dbo.Task;
drop table if exists dbo.Message;
drop table if exists dbo.Chat;
drop table if exists dbo.UserParticipatesInEvent;
drop table if exists dbo.UserParticipatesInEventChat;

create table dbo.Users
(
    id                  serial primary key,
    name            VARCHAR(64)         not null,
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

CREATE TABLE dbo.Event (
   id          serial primary key,
   name        VARCHAR(64) NOT NULL,
   description VARCHAR(512),
   category    VARCHAR(64),
   location    VARCHAR(255),
   visibility  VARCHAR(64),
   date        TIMESTAMP
);

CREATE TABLE dbo.Task (
  id          serial primary key,
  name        VARCHAR(255) NOT NULL,
  description VARCHAR(512),
  event_id    INT not null,
  user_id     INT,
  FOREIGN KEY (event_id) REFERENCES dbo.Event(id),
  FOREIGN KEY (user_id) REFERENCES dbo.Users(id)
);

CREATE TABLE dbo.Chat (
  id        serial primary key,
  name      VARCHAR(255) NOT NULL,
  event_id  INT not null,
  FOREIGN KEY (event_id) REFERENCES dbo.Event(id)
);

CREATE TABLE dbo.Message (
  id        serial primary key,
  text      VARCHAR(512),
  time      TIMESTAMP,
  sender_id INT not null,
  chat_id   INT not null,
  FOREIGN KEY (sender_id) REFERENCES dbo.Users(id),
  FOREIGN KEY (chat_id) REFERENCES dbo.Chat(id)
);

CREATE TABLE dbo.UserParticipatesInEvent (
  user_id  INT not null,
  event_id INT not null,
  PRIMARY KEY (user_id, event_id),
  FOREIGN KEY (user_id) REFERENCES dbo.Users(id),
  FOREIGN KEY (event_id) REFERENCES dbo.Event(id)
);
