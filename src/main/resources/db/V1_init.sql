CREATE TABLE USERS
(
    ID           BIGSERIAL PRIMARY KEY,
    FIRST_NAME   VARCHAR NOT NULL,
    LAST_NAME    VARCHAR NOT NULL
);

CREATE TABLE TUSICH
(
    ID      BIGSERIAL    PRIMARY KEY,
    NAME    VARCHAR      NOT NULL
);

CREATE TABLE PARTICIPATION
(
    ID        BIGSERIAL      PRIMARY KEY,
    USER_ID   BIGSERIAL      NOT NULL REFERENCES USERS (ID) ON DELETE CASCADE,
    TUSICH_ID BIGSERIAL      NOT NULL REFERENCES TUSICH (ID) ON DELETE CASCADE
);

CREATE TABLE ACHIEVEMENT
(
    ID         BIGSERIAL PRIMARY KEY,
    TUSICH_ID  BIGINT NOT NULL REFERENCES TUSICH (id) ON DELETE CASCADE,
    NAME       VARCHAR NOT NULL
);

CREATE TABLE USER_ACHIEVEMENT
(
    ID         BIGSERIAL PRIMARY KEY,
    USER_ID    BIGINT NOT NULL REFERENCES USERS (id) ON DELETE CASCADE,
    ACHIEVEMENT_ID  BIGINT NOT NULL REFERENCES ACHIEVEMENT (id) ON DELETE CASCADE,
    LEVEL      BIGINT DEFAULT 1
);
