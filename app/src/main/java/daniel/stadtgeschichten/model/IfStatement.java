package daniel.stadtgeschichten.model;

import java.util.Arrays;

/**
 * This class represents an if-then-else or an if-then statement.
 */
public class IfStatement extends AbsStatement
{
    /**
     * Conditions that should be checked
     */
    private AbsOperator[] conditions;

    /**
     * Statements that are executed when the conditions are fulfilled
     */
    private AbsStatement[] thenStatements;

    /**
     * Statements that are executed when the conditions are not fulfilled
     */
    private AbsStatement[] elseStatements;

    public IfStatement(AbsOperator[] conditions, AbsStatement[] thenStatements,
                       AbsStatement[] elseStatements)
    {
        this.conditions = conditions;
        this.thenStatements = thenStatements;
        this.elseStatements = elseStatements;
    }

    /**
     * @return {@link IfStatement#conditions}
     */
    public AbsOperator[] getConditions()
    {
        return conditions;
    }

    /**
     * @return {@link IfStatement#thenStatements}
     */
    public AbsStatement[] getThenStatements()
    {
        return thenStatements;
    }

    /**
     * @return {@link IfStatement#elseStatements}
     */
    public AbsStatement[] getElseStatements()
    {
        return elseStatements;
    }

    @Override
    public String toString()
    {
        return "IfStatement{" +
                "conditions=" + Arrays.toString(conditions) +
                ", thenStatements=" + Arrays.toString(thenStatements) +
                ", elseStatements=" + Arrays.toString(elseStatements) +
                '}';
    }
}
