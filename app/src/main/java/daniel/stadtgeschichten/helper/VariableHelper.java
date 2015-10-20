package daniel.stadtgeschichten.helper;

import java.util.HashMap;

import daniel.stadtgeschichten.model.AssignmentStatement;

/**
 * This helper class is used to manage used variables.
 */
public class VariableHelper
{
    /**
     * Variable names mapped to their value
     */
    private HashMap<String, Integer> variables = new HashMap<>();

    /**
     * Get the value associated with the given variable name.
     * @param variable variable name
     * @return value associated with the variable name
     */
    public int getValue(String variable)
    {
        return variables.get(variable);
    }

    /**
     * Store the variable name with the value. This may override an existing variable with the same
     * name.
     * @param variable variable name
     * @param value value
     */
    public void setVariable(String variable, int value)
    {
        variables.put(variable, value);
    }

    /**
     * Check whether there exists a variable with the given name.
     * @param variable variable name
     * @return true if a variable with the given name exists, false otherwise
     */
    public boolean hasVariable(String variable)
    {
        return variables.containsKey(variable);
    }

    /**
     * Remove the variable with the given name.
     * @param variable variable name
     */
    public void removeVariable(String variable)
    {
        variables.remove(variable);
    }

    /**
     * Delete all variables.
     */
    public void clear()
    {
        variables.clear();
    }

    /**
     * Process assignment statements.
     * @param statements assignment statements
     */
    public void processStatements(AssignmentStatement[] statements)
    {
        for (AssignmentStatement statement : statements)
            setVariable(statement.getVariable(), statement.getValue());
    }
}
