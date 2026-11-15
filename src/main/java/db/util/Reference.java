package db.util;

import db.Table;

import java.io.Serializable;

public record Reference(Table table, String field) implements Serializable {}
