EVENTS DROP TABLE IF EXISTS EVENTS;
CREATE TABLE EVENTS(ID INT PRIMARY KEY, NAME VARCHAR(255),
 TYPE VARCHAR(255),
START_TIME TIMESTAMP,
END_TIME TIMESTAMP,
DATE DATE
);