package entertainment;

import java.util.ArrayList;

public class Show extends Video {
    private ArrayList<Season> seasons;

    public Show(String title, int releaseYear) {
        super(title, releaseYear);
        seasons = new ArrayList<>();
    }

    public Show(String title, int releaseYear, ArrayList<Genre> genres) {
        super(title, releaseYear, genres);
    }
}
