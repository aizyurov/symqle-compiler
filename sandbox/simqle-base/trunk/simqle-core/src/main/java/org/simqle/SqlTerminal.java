/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle;

/**
 * <br/>16.03.2010
 *
 * @author Alexander Izyurov
 */
public enum SqlTerminal implements Sql {

    /**************************
     * Special characters
     */

    /**
     * &lt;not equals operator&gt; ::= &lt;&gt;
     */
      NE("<>"),
    /**
     * &lt;greater than or equals operator&gt; ::= &gt;
     */
       GE(">="),
    /**
     * &lt;less than or equals operator&gt; ::= &lt;
     */
       LE("<="),
    /**
     * &lt;concatenation operator&gt ::= ||
     */
       CONCAT("||"),
    /**
     * &lt;double period&gt; ::= ..
     */
       DOUBLE_PERIOD(".."),
    /**
     * &lt;double quote&gt; ::= "
     */
       DOUBLE_QUOTE("\""),
    /**
     * &lt;percent&gt; ::= %
     */
       PERCENT("%"),
    /**
     * &lt;ampersand&gt; ::= &
     */
       AMPERSAND("&"),
    /**
     * &lt;quote&gt; ::= '
     */
       QUOTE("'"),
    /**
     * &lt;left paren&gt; ::= (
     */
       LEFT_PAREN("("),
    /**
     * &lt;right paren&gt; ::= )
     */
       RIGHT_PAREN(")"),
    /**
     * &lt;asterisk&gt; ::= *
     */
       ASTERISK("*"),
    /**
     * &lt;plus sign&gt; ::= +
     */
       PLUS("+"),
    /**
     * &lt;comma&gt; ::= ,
     */
       COMMA(","),
    /**
     * &lt;minus sign&gt; ::= -
     */
       MINUS("-"),
    /**
     * &lt;period&gt; ::= .
     */
       PERIOD("."),
    /**
     * &lt;solidus&gt; ::= /
     */
       SOLIDUS("/"),
    /**
     * &lt;colon&gt; ::= :
     */
       COLON(":"),
    /**
     * &lt;semicolon&gt; ::= ;
     */
       SEMICOLON(";"),
    /**
     * &lt;less than operator&gt; ::= &lt;
     */
       LT("<"),
    /**
     * &lt;equals operator&gt; ::= =
     */
       EQ("="),
    /**
     * &lt;greater than operator&gt; ::= &gt;
     */
       GT(">"),
    /**
     * &lt;question mark&gt; ::= ?
     */
       QUESTION("?"),
    /**
     * &lt;left bracket&gt; ::= [
     */
       LEFT_BRACKET("["),
    /**
     * &lt;right bracket&gt; ::= ]
     */
       RIGHT_BRACKET("]"),
    /**
     * &lt;underscore&gt; ::= _
     */
       UNDERSCORE("_"),
    /**
     * &lt;vertical bar&gt; ::= |
     */
     VERTICAL_BAR("|"),

    /******************************
     * keywords
     */

    //          <key word> ::=
    //                <reserved word>
    //          | <non-reserved word>

    //     <non-reserved word> ::=
    ADA, C, CATALOG_NAME , CHARACTER_SET_CATALOG , CHARACTER_SET_NAME
    , CHARACTER_SET_SCHEMA , CLASS_ORIGIN , COBOL , COLLATION_CATALOG
    , COLLATION_NAME , COLLATION_SCHEMA , COLUMN_NAME , COMMAND_FUNCTION , COMMITTED
    , CONDITION_NUMBER , CONNECTION_NAME , CONSTRAINT_CATALOG , CONSTRAINT_NAME
    , CONSTRAINT_SCHEMA , CURSOR_NAME
    , DATA , DATETIME_INTERVAL_CODE , DATETIME_INTERVAL_PRECISION , DYNAMIC_FUNCTION
    , FORTRAN
    , LENGTH
    , MESSAGE_LENGTH , MESSAGE_OCTET_LENGTH , MESSAGE_TEXT , MORE , MUMPS,

    //    <reserved word> ::=
    ABSOLUTE , ACTION , ADD , ALL , ALLOCATE , ALTER , AND
                 , ANY , ARE , AS , ASC
                 , ASSERTION , AT , AUTHORIZATION , AVG
                 , BEGIN , BETWEEN , BIT , BIT_LENGTH , BOTH , BY
                 , CASCADE , CASCADED , CASE , CAST , CATALOG , CHAR , CHARACTER , CHAR_LENGTH
                 , CHARACTER_LENGTH , CHECK , CLOSE , COALESCE , COLLATE , COLLATION
                 , COLUMN , COMMIT , CONNECT , CONNECTION , CONSTRAINT
                 , CONSTRAINTS , CONTINUE
                 , CONVERT , CORRESPONDING , COUNT , CREATE , CROSS , CURRENT
                 , CURRENT_DATE , CURRENT_TIME , CURRENT_TIMESTAMP , CURRENT_USER , CURSOR
                 , DATE , DAY , DEALLOCATE , DEC , DECIMAL , DECLARE , DEFAULT , DEFERRABLE
                 , DEFERRED , DELETE , DESC , DESCRIBE , DESCRIPTOR , DIAGNOSTICS
                 , DISCONNECT , DISTINCT , DOMAIN , DOUBLE , DROP
                 , ELSE , END , END_EXEC("END-EXEC"), ESCAPE , EXCEPT , EXCEPTION
                 , EXEC , EXECUTE , EXISTS
                 , EXTERNAL , EXTRACT
                 , FALSE , FETCH , FIRST , FLOAT , FOR , FOREIGN , FOUND , FROM , FULL
                 , GET , GLOBAL , GO , GOTO , GRANT , GROUP
                 , HAVING , HOUR
                 , IDENTITY , IMMEDIATE , IN , INDICATOR , INITIALLY , INNER , INPUT
                 , INSENSITIVE , INSERT , INT , INTEGER , INTERSECT , INTERVAL , INTO , IS
                 , ISOLATION
                 , JOIN
                 , KEY
                 , LANGUAGE , LAST , LEADING , LEFT , LEVEL , LIKE , LOCAL , LOWER

                 , MATCH , MAX , MIN , MINUTE , MODULE , MONTH
                 , NAMES , NATIONAL , NATURAL , NCHAR , NEXT , NO , NOT , NULL, NULLS

                 , NULLIF , NUMERIC
                 , OCTET_LENGTH , OF , ON , ONLY , OPEN , OPTION , OR
                 , ORDER , OUTER
                 , OUTPUT , OVERLAPS
                 , PAD , PARTIAL , POSITION , PRECISION , PREPARE , PRESERVE , PRIMARY
                  , PRIOR , PRIVILEGES , PROCEDURE , PUBLIC
                  , READ , REAL , REFERENCES , RELATIVE , RESTRICT , REVOKE , RIGHT
                  , ROLLBACK , ROWS
                  , SCHEMA , SCROLL , SECOND , SECTION , SELECT , SESSION , SESSION_USER , SET
                  , SIZE , SMALLINT , SOME , SPACE , SQL , SQLCODE , SQLERROR , SQLSTATE
                  , SUBSTRING , SUM , SYSTEM_USER
                  , TABLE , TEMPORARY , THEN , TIME , TIMESTAMP , TIMEZONE_HOUR , TIMEZONE_MINUTE
                  , TO , TRAILING , TRANSACTION , TRANSLATE , TRANSLATION , TRIM , TRUE
                  , UNION , UNIQUE , UNKNOWN , UPDATE , UPPER , USAGE , USER , USING
                  , VALUE , VALUES , VARCHAR , VARYING , VIEW
                  , WHEN , WHENEVER , WHERE , WITH , WORK , WRITE
                  , YEAR
                  , ZONE,

    // additional list reserved for future extensions
    AFTER, ALIAS, ASYNC, BEFORE, BOOLEAN, BREADTH,
         COMPLETION, CALL, CYCLE, DEPTH, DICTIONARY, EACH, ELSEIF,
         EQUALS, GENERAL, IF, IGNORE, LEAVE, LESS, LIMIT, LOOP, MODIFY,
         NEW, NONE, OBJECT, OFF, OID, OLD, OPERATION, OPERATORS, OTHERS,
         PARAMETERS, PENDANT, PREORDER, PRIVATE, PROTECTED, RECURSIVE, REF,
         REFERENCING, REPLACE, RESIGNAL, RETURN, RETURNS, ROLE, ROUTINE,
         ROW, SAVEPOINT, SEARCH, SENSITIVE, SEQUENCE, SIGNAL, SIMILAR,
         SQLEXCEPTION, SQLWARNING, STRUCTURE, TEST, THERE, TRIGGER, TYPE,
         UNDER, VARIABLE, VIRTUAL, VISIBLE, WAIT, WHILE, WITHOUT
    ;

    SqlTerminal() {
        s = super.toString();
    }

    private String s;

    SqlTerminal(String s) {
        this.s=s;
    }

    public String getSqlText() {
        return s;
    }

    public void setParameters(SqlParameters p) {
        // do nothing
    }

}
