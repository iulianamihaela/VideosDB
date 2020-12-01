package actions.recommendations;

import fileio.ActionInputData;
import fileio.Writer;
import org.json.simple.JSONObject;

import java.io.IOException;

public class RecommendationUtils {
    public static JSONObject recommendationFailure(final ActionInputData actionInput,
                                                   final Writer writer,
                                                   final String typeOfRecommendation) {
        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    typeOfRecommendation + " cannot be applied!");
        } catch (IOException e) {
            e.printStackTrace();

            return new JSONObject();
        }
    }
}
