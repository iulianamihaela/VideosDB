package actions.recommendations;

import fileio.ActionInputData;
import fileio.Writer;
import org.json.simple.JSONObject;

import java.io.IOException;

final class RecommendationUtils {
    private RecommendationUtils() { }

    /**
     * Returns a recommendation failure message as JSON Object
     *
     * @param actionInput          action input data
     * @param writer               output writer
     * @param typeOfRecommendation type of recommendation
     * @return recommendation failure message as JSONObject
     */
    static JSONObject recommendationFailure(final ActionInputData actionInput,
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
