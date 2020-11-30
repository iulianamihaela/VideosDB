package common;

public class EntityWithSortingCriteria implements Comparable {
    private String name;
    private Double sortingCriteria;

    public EntityWithSortingCriteria(final String name, final Double rating) {
        this.name = name;
        this.sortingCriteria = rating;
    }

    /**
     * Compares to another object
     * @param o object to be compared with
     * @return order
     */
    @Override
    public int compareTo(final Object o) {
        if (o instanceof EntityWithSortingCriteria) {
            if (Double.compare(this.sortingCriteria,
                    ((EntityWithSortingCriteria) o).sortingCriteria) == 0) {
                return this.name.compareTo(((EntityWithSortingCriteria) o).name);
            }

            return Double.compare(this.sortingCriteria,
                    ((EntityWithSortingCriteria) o).sortingCriteria);
        }
        return 0;
    }

    /**
     * Returns user's name as string
     * @return user's name
     */
    @Override
    public String toString() {
        return this.name;
    }
}
