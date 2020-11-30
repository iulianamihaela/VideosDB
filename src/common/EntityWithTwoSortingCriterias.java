package common;

public class EntityWithTwoSortingCriterias implements Comparable {
    private String name;
    private Double firstSortingCriteria;
    private Double secondSortingCriteria;

    public EntityWithTwoSortingCriterias(final String givenName,
                                         final Double givenSortingCriteria,
                                         final Double givenSecondSortingCriteria) {
        name = givenName;
        firstSortingCriteria = givenSortingCriteria;
        secondSortingCriteria = givenSecondSortingCriteria;
    }

    /**
     * Compares to another object
     * @param o object to be compared with
     * @return order
     */
    @Override
    public int compareTo(final Object o) {
        if (o instanceof EntityWithTwoSortingCriterias) {
            int firstDiff = (int) (this.firstSortingCriteria
                    - ((EntityWithTwoSortingCriterias) o).firstSortingCriteria);

            if (firstDiff == 0) {
                return (int) (this.secondSortingCriteria
                        - ((EntityWithTwoSortingCriterias) o).secondSortingCriteria);
            }

            return firstDiff;
        }
        return 0;
    }

    /**
     * Returns video's name as string
     * @return video's name
     */
    @Override
    public String toString() {
        return this.name;
    }
}
