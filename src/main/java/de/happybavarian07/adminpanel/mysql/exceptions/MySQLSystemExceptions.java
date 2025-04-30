package de.happybavarian07.adminpanel.mysql.exceptions;

public class MySQLSystemExceptions {
    // TODO Implement into the system later
    public static class DatabaseConnectionException extends Exception {
        public DatabaseConnectionException(String message, Throwable cause) {
            super(message, cause);
        }

        public DatabaseConnectionException(String message) {
            super(message);
        }

        public DatabaseConnectionException(Throwable cause) {
            super(cause);
        }
    }

    public static class TableCreationException extends Exception {
        public TableCreationException(String message, Throwable cause) {
            super(message, cause);
        }

        public TableCreationException(String message) {
            super(message);
        }

        public TableCreationException(Throwable cause) {
            super(cause);
        }
    }

    public static class QueryExecutionException extends Exception {
        public QueryExecutionException(String message, Throwable cause) {
            super(message, cause);
        }

        public QueryExecutionException(String message) {
            super(message);
        }

        public QueryExecutionException(Throwable cause) {
            super(cause);
        }
    }

    public static class OutputConversionException extends Exception {
        public OutputConversionException(String message, Throwable cause) {
            super(message, cause);
        }

        public OutputConversionException(String message) {
            super(message);
        }

        public OutputConversionException(Throwable cause) {
            super(cause);
        }
    }

    public static class ReflectionOperationException extends Exception {
        public ReflectionOperationException(String message, Throwable cause) {
            super(message, cause);
        }

        public ReflectionOperationException(String message) {
            super(message);
        }

        public ReflectionOperationException(Throwable cause) {
            super(cause);
        }
    }

    public static class FileOperationException extends Exception {
        public FileOperationException(String message, Throwable cause) {
            super(message, cause);
        }

        public FileOperationException(String message) {
            super(message);
        }

        public FileOperationException(Throwable cause) {
            super(cause);
        }
    }

    public static class InvalidSQLAnnotationException extends Exception {
        public InvalidSQLAnnotationException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidSQLAnnotationException(String message) {
            super(message);
        }

        public InvalidSQLAnnotationException(Throwable cause) {
            super(cause);
        }
    }

    public static class InvalidSQLColumnException extends Exception {
        public InvalidSQLColumnException(String message) {
            super(message);
        }

        public InvalidSQLColumnException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidSQLColumnException(Throwable cause) {
            super(cause);
        }
    }

    public static class InvalidSQLActionException extends Exception {
        public InvalidSQLActionException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidSQLActionException(String message) {
            super(message);
        }

        public InvalidSQLActionException(Throwable cause) {
            super(cause);
        }
    }

    public static class InvalidSQLTableException extends Exception {
        public InvalidSQLTableException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidSQLTableException(String message) {
            super(message);
        }

        public InvalidSQLTableException(Throwable cause) {
            super(cause);
        }
    }

    public static class InvalidSQLTypeException extends Exception {
        public InvalidSQLTypeException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidSQLTypeException(String message) {
            super(message);
        }

        public InvalidSQLTypeException(Throwable cause) {
            super(cause);
        }
    }
}
