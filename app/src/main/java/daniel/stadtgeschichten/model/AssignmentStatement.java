package daniel.stadtgeschichten.model;

/**
 * This class represents the assignment statement.
 */
public class AssignmentStatement extends AbsStatement
{
    /**
     * Variable name
     */
    private String variable;

    /**
     * Value to assign to the variable
     */
    private int value;

    public AssignmentStatement(String variable, int value)
    {
        this.variable = variable;
        this.value = value;
    }

    /**
     * @return {@Link AssignmentStatement#variable}
     */
    public String getVariable()
    {
        return variable;
    }

    /**
     * @return {@link AssignmentStatement#value}
     */
    public int getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "AssignmentStatement{" +
                "variable='" + variable + '\'' +
                ", value=" + value +
                '}';
    }
}
