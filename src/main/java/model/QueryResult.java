package model;

public record QueryResult(boolean isSuccess, String errorMessage, String[] headers, Object[][] data, long execution) {}
