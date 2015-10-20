package daniel.stadtgeschichten.model;

/**
 * This class represents the incrementation statement.
 */
public class IncrementStatement extends AbsStatement
{
    /**
     * Variable name
     */
    private String variable;

    /**
     * Value to increase the variable with
     */
    private int value = 1;

    public IncrementStatement(String variable)
    {
        this.variable = variable;
    }

    public IncrementStatement(String variable, int value)
    {
        this.variable = variable;
        this.value = value;
    }

    /**
     * @return {@Link IncrementStatement#variable}
     */
    public String getVariable()
    {
        return variable;
    }

    /**
     * @return {@link IncrementStatement#value}
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
