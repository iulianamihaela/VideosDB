package common;

public class ActorWithSortingCriteria implements Comparable {
    private String name;
    private Double sortingCriteria;

    public ActorWithSortingCriteria(final String name, final Double rating) {
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
        if (o instanceof ActorWithSortingCriteria) {
            int diff = (int) (this.sortingCriteria
                    - ((ActorWithSortingCriteria) o).sortingCriteria);

            if (diff == 0) {
                return this.name.compareTo(((ActorWithSortingCriteria) o).name);
            }

            return diff;
        }
        return 0;
    }

    /**
     * Returns actor's name as string
     * @return actor's name
     */
    @Override
    public String toString() {
        return this.name;
    }
}
