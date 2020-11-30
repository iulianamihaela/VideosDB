package common;

public class VideoWithSortingCriteria implements Comparable {
    private String name;
    private Double sortingCriteria;

    public VideoWithSortingCriteria(final String givenName, final Double givenSortingCriteria) {
        name = givenName;
        sortingCriteria = givenSortingCriteria;
    }

    /**
     * Compares to another object
     * @param o object to be compared with
     * @return order
     */
    @Override
    public int compareTo(final Object o) {
        if (o instanceof VideoWithSortingCriteria) {
            int diff = (int) (this.sortingCriteria
                    - ((VideoWithSortingCriteria) o).sortingCriteria);

            if (diff == 0) {
                return this.name.compareTo(((VideoWithSortingCriteria) o).name);
            }

            return diff;
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
