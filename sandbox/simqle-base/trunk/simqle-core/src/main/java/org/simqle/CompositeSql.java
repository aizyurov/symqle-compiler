/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle;


/**
 * Represents an Sql element composed from a list of sub-elements.
 * Provides implementation of {@link #getSqlText()} and {@link #setParameters(SqlParameters)}}
 * @author Alexander Izyurov
 */
public class CompositeSql implements Sql {
    /**
     * the first sub-element.
     */
    private final Sql first;
    /**
     * other sub-elements.
     */
    private final Sql[] other;

    /**
     * Constructs composite Sql from elements.
     * @param first the first element of sequence, not null
     * @param other elements, optional (but each not null)
     */
    public CompositeSql(final Sql first, final Sql... other) {
        this.first = first;
        this.other = other != null ? other : new Sql[0];
    }


    /**
     * Constructs Sql text as concatenation of Sql text of elements.
     * @return constructed text
     */
    public final String getSqlText() {
        // minor optimization
        if (other.length==0) {
            return first.getSqlText();
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append(first.getSqlText());
            for (Sql element : this.other) {
                if (builder.length()>0) {
                    builder.append(' ');
                }
                builder.append(element.getSqlText());
            }
            return builder.toString();
        }
    }

    /**
     * Sets SqlParameters by delegation to each member in turn.
     * @param p SqlParameters interface to write parameter values into
     */
    public final void setParameters(final SqlParameters p) {
        first.setParameters(p);
        for (Sql element : this.other) {
            element.setParameters(p);
        }
    }


}
