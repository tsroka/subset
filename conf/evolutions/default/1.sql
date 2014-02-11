# --- !Ups

CREATE TABLE Rule (
    id bigint NOT NULL AUTO_INCREMENT,
    name varchar(32) NOT NULL,
    ruleJson blob NOT NULL,
    created timestamp NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE CsvData (
  name varchar(32) NOT NULL,
  content blob NOT NULL,
  ruleId bigint NOT NULL,
  created timestamp NOT NULL,
  PRIMARY KEY (name),
  FOREIGN KEY (ruleId) REFERENCES Rule(id)
);

