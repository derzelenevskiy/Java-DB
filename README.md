# Java-DB

Simple in-memory DBMS made with pure Java. Features SQL-like commands and terminal UI

***

Valid requests:

- ```CREATE TABLE <table_name> (<column> <type> [PRIMARY KEY] [REFERENCES <ref_table>], ...);```
- ```DROP TABLE <table_name>;```
- ```INSERT INTO <table_name> VALUES (<val1>, <val2>, ...) [, (...)];```
- ```INSERT INTO <table_name> (<col1>, <col2>, ...) VALUES (<val1>, <val2>, ...) [, (...)];```
- ```SELECT * FROM <table_name> [WHERE <column> <operator> <value>];```
- ```SELECT <col1>, <col2>, ... FROM <table_name> [WHERE <column> <operator> <value>];```
- ```UPDATE <table_name> SET <col1> = <val1>, ... [WHERE <column> <operator> <value>];```
- ```DELETE FROM <table_name> [WHERE <column> <operator> <value>];```

Supported types: `INT` / `INTEGER`, `DOUBLE` / `FLOAT`, `STRING`, `DATE` (`'YYYY-MM-DD'`), `BOOLEAN` / `BOOL`

Supported operators: `=`, `!=`, `<`, `>`, `<=`, `>=`

`STRING` and `DATE` values must be enclosed in single quotes (`'...'`). Every query must end with `;`.

## Building and running from source

Build the package with maven then run ```Java-DB-1.0-SNAPSHOT.jar``` in ```./target```