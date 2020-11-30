package common;

public class UserWithSortingCriteria implements Comparable {
    private String name;
    private Double sortingCriteria;

    public UserWithSortingCriteria(final String name, final Double rating) {
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
        if (o instanceof UserWithSortingCriteria) {
            int diff = (int) (this.sortingCriteria
                    - ((UserWithSortingCriteria) o).sortingCriteria);

            if (diff == 0) {
                return this.name.compareTo(((UserWithSortingCriteria) o).name);
            }

            return diff;
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
