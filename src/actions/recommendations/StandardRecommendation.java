package actions.recommendations;

import entertainment.Serial;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;

import java.io.IOException;

public class StandardRecommendation {
    public static JSONObject execute(final Database database,
                               final ActionInputData actionInput,
                               final Writer writer) {
        String result = "";

        for (String title : database.getVideosOrder()) {
            if (database.getMovies().containsKey(title)
                    && !database.getMovies().get(title)
                    .hasBeenViewedByUser(actionInput.getUsername())) {
                result = title;
                break;
            } else if (database.getSerials().containsKey(title)
                    && !database.getSerials().get(title)
                    .hasBeenViewedByUser(actionInput.getUsername())) {
                result = title;
                break;
            }
        }

        if (result.isEmpty()) {
            for (Serial serial : database.getSerials().values()) {
                if (!serial.hasBeenViewedByUser(actionInput.getUsername())) {
                    result = serial.getTitle();
                }
            }
        }

        if (!result.isEmpty()) {
            try {
                return writer.writeFile(actionInput.getActionId(),
                        "message",
                        "StandardRecommendation result: "
                                + result
                );
            } catch (IOException e) {
                e.printStackTrace();

                return new JSONObject();
            }
        }

        return RecommendationUtils.recommendationFailure(actionInput,
                writer,
                "StandardRecommendation");
    }
}
