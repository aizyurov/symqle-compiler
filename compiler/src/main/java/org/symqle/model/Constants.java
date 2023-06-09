/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
*/

package org.symqle.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Terminal symbols of SQL.
 */
public final class Constants {

    private Constants() {
    }

    // to make both Cobertura and Checkstyle happy - private constructor is covered
    static {
        new Constants();
    }

    private static final String[] constants = {
        "NE",
        "GE",
        "LE",
        "CONCAT",
        "DOUBLE_PERIOD",
        "DOUBLE_QUOTE",
        "PERCENT",
        "AMPERSAND",
        "QUOTE",
        "LEFT_PAREN",
        "RIGHT_PAREN",
        "ASTERISK",
        "PLUS",
        "COMMA",
        "MINUS",
        "PERIOD",
        "SOLIDUS",
        "COLON",
        "SEMICOLON",
        "LT",
        "EQ",
        "GT",
        "QUESTION",
        "LEFT_BRACKET",
        "RIGHT_BRACKET",
        "UNDERSCORE",
        "VERTICAL_BAR",
        "ADA",
        "C",
        "CATALOG_NAME",
        "CHARACTER_SET_CATALOG",
        "CHARACTER_SET_NAME",
        "CHARACTER_SET_SCHEMA",
        "CLASS_ORIGIN",
        "COBOL",
        "COLLATION_CATALOG",
        "COLLATION_NAME",
        "COLLATION_SCHEMA",
        "COLUMN_NAME",
        "COMMAND_FUNCTION",
        "COMMITTED",
        "CONDITION_NUMBER",
        "CONNECTION_NAME",
        "CONSTRAINT_CATALOG",
        "CONSTRAINT_NAME",
        "CONSTRAINT_SCHEMA",
        "CURSOR_NAME",
        "DATA",
        "DATETIME_INTERVAL_CODE",
        "DATETIME_INTERVAL_PRECISION",
        "DYNAMIC_FUNCTION",
        "FORTRAN",
        "LENGTH",
        "MESSAGE_LENGTH",
        "MESSAGE_OCTET_LENGTH",
        "MESSAGE_TEXT",
        "MORE",
        "MUMPS",
        "ABSOLUTE",
        "ACTION",
        "ADD",
        "ALL",
        "ALLOCATE",
        "ALTER",
        "AND",
        "ANY",
        "ARE",
        "AS",
        "ASC",
        "ASSERTION",
        "AT",
        "AUTHORIZATION",
        "AVG",
        "BEGIN",
        "BETWEEN",
        "BIT",
        "BIT_LENGTH",
        "BOTH",
        "BY",
        "CASCADE",
        "CASCADED",
        "CASE",
        "CAST",
        "CATALOG",
        "CHAR",
        "CHARACTER",
        "CHAR_LENGTH",
        "CHARACTER_LENGTH",
        "CHECK",
        "CLOSE",
        "COALESCE",
        "COLLATE",
        "COLLATION",
        "COLUMN",
        "COMMIT",
        "CONNECT",
        "CONNECTION",
        "CONSTRAINT",
        "CONSTRAINTS",
        "CONTINUE",
        "CONVERT",
        "CORRESPONDING",
        "COUNT",
        "CREATE",
        "CROSS",
        "CURRENT",
        "CURRENT_DATE",
        "CURRENT_TIME",
        "CURRENT_TIMESTAMP",
        "CURRENT_USER",
        "CURSOR",
        "DATE",
        "DAY",
        "DEALLOCATE",
        "DEC",
        "DECIMAL",
        "DECLARE",
        "DEFAULT",
        "DEFERRABLE",
        "DEFERRED",
        "DELETE",
        "DESC",
        "DESCRIBE",
        "DESCRIPTOR",
        "DIAGNOSTICS",
        "DISCONNECT",
        "DISTINCT",
        "DOMAIN",
        "DOUBLE",
        "DROP",
        "ELSE",
        "END",
        "END_EXEC",
        "ESCAPE",
        "EXCEPT",
        "EXCEPTION",
        "EXEC",
        "EXECUTE",
        "EXISTS",
        "EXTERNAL",
        "EXTRACT",
        "FALSE",
        "FETCH",
        "FIRST",
        "FLOAT",
        "FOR",
        "FOREIGN",
        "FOUND",
        "FROM",
        "FULL",
        "GET",
        "GLOBAL",
        "GO",
        "GOTO",
        "GRANT",
        "GROUP",
        "HAVING",
        "HOUR",
        "IDENTITY",
        "IMMEDIATE",
        "IN",
        "INDICATOR",
        "INITIALLY",
        "INNER",
        "INPUT",
        "INSENSITIVE",
        "INSERT",
        "INT",
        "INTEGER",
        "INTERSECT",
        "INTERVAL",
        "INTO",
        "IS",
        "ISOLATION",
        "JOIN",
        "KEY",
        "LANGUAGE",
        "LAST",
        "LEADING",
        "LEFT",
        "LEVEL",
        "LIKE",
        "LOCAL",
        "LOWER",
        "MATCH",
        "MAX",
        "MIN",
        "MINUTE",
        "MODULE",
        "MONTH",
        "NAMES",
        "NATIONAL",
        "NATURAL",
        "NCHAR",
        "NEXT",
        "NO",
        "NOT",
        "NULL",
        "NULLS",
        "NULLIF",
        "NUMERIC",
        "OCTET_LENGTH",
        "OF",
        "ON",
        "ONLY",
        "OPEN",
        "OPTION",
        "OR",
        "ORDER",
        "OUTER",
        "OUTPUT",
        "OVERLAPS",
        "PAD",
        "PARTIAL",
        "POSITION",
        "PRECISION",
        "PREPARE",
        "PRESERVE",
        "PRIMARY",
        "PRIOR",
        "PRIVILEGES",
        "PROCEDURE",
        "PUBLIC",
        "READ",
        "REAL",
        "REFERENCES",
        "RELATIVE",
        "RESTRICT",
        "REVOKE",
        "RIGHT",
        "ROLLBACK",
        "ROWS",
        "SCHEMA",
        "SCROLL",
        "SECOND",
        "SECTION",
        "SELECT",
        "SESSION",
        "SESSION_USER",
        "SET",
        "SIZE",
        "SMALLINT",
        "SOME",
        "SPACE",
        "SQL",
        "SQLCODE",
        "SQLERROR",
        "SQLSTATE",
        "SUBSTRING",
        "SUM",
        "SYSTEM_USER",
        "TABLE",
        "TEMPORARY",
        "THEN",
        "TIME",
        "TIMESTAMP",
        "TIMEZONE_HOUR",
        "TIMEZONE_MINUTE",
        "TO",
        "TRAILING",
        "TRANSACTION",
        "TRANSLATE",
        "TRANSLATION",
        "TRIM",
        "TRUE",
        "UNION",
        "UNIQUE",
        "UNKNOWN",
        "UPDATE",
        "UPPER",
        "USAGE",
        "USER",
        "USING",
        "VALUE",
        "VALUES",
        "VARCHAR",
        "VARYING",
        "VIEW",
        "WHEN",
        "WHENEVER",
        "WHERE",
        "WITH",
        "WORK",
        "WRITE",
        "YEAR",
        "ZONE",
        "AFTER",
        "ALIAS",
        "ASYNC",
        "BEFORE",
        "BOOLEAN",
        "BREADTH",
        "COMPLETION",
        "CALL",
        "CYCLE",
        "DEPTH",
        "DICTIONARY",
        "EACH",
        "ELSEIF",
        "EQUALS",
        "GENERAL",
        "IF",
        "IGNORE",
        "LEAVE",
        "LESS",
        "LIMIT",
        "LOOP",
        "MODIFY",
        "NEW",
        "NONE",
        "OBJECT",
        "OFF",
        "OFFSET",
        "OID",
        "OLD",
        "OPERATION",
        "OPERATORS",
        "OTHERS",
        "PARAMETERS",
        "PENDANT",
        "PREORDER",
        "PRIVATE",
        "PROTECTED",
        "RECURSIVE",
        "REF",
        "REFERENCING",
        "REPLACE",
        "RESIGNAL",
        "RETURN",
        "RETURNS",
        "ROLE",
        "ROUTINE",
        "ROW",
        "SAVEPOINT",
        "SEARCH",
        "SENSITIVE",
        "SEQUENCE",
        "SIGNAL",
        "SIMILAR",
        "SQLEXCEPTION",
        "SQLWARNING",
        "STRUCTURE",
        "TEST",
        "THERE",
        "TRIGGER",
        "TYPE",
        "UNDER",
        "VARIABLE",
        "VIRTUAL",
        "VISIBLE",
        "WAIT",
        "WHILE",
        "WITHOUT"
    };

    private static final Set<String> constantsSet = new HashSet<String>(Arrays.asList(constants));

    /**
     * Determines whether {@code s} is a known constant.
     * @param s the value to check
     * @return true if known constant.
     */
    public static boolean isConstant(final String s) {
        return constantsSet.contains(s);
    }
}
