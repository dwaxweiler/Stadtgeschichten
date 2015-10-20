package daniel.stadtgeschichten.model;

/**
 * This class represents the equality operator.
 */
public class EqualityOperator extends AbsOperator
{
    /**
     * First element to compare
     */
    private String element1;

    /**
     * Second element to compare
     */
    private String element2;

    public EqualityOperator(String element1, String element2)
    {
        this.element1 = element1;
        this.element2 = element2;
    }

    /**
     * @return {@link EqualityOperator#element1}
     */
    public String getElement1()
    {
        return element1;
    }

    /**
     * @return {@link EqualityOperator#element2}
     */
    public String getElement2()
    {
        return element2;
    }

    @Override
    public String toString()
    {
        return "EqualityOperator{" +
                "element1='" + element1 + '\'' +
                ", element2='" + element2 + '\'' +
                '}';
    }
}
