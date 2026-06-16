package commands;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NasaCommandHandlerTest {

    @Test
    void detectsImageMediaType() {
        JsonObject json = new JsonObject();
        json.addProperty("media_type", "image");

        assertTrue(NasaCommandHandler.isImage(json));
    }

    @Test
    void rejectsVideoMediaType() {
        JsonObject json = new JsonObject();
        json.addProperty("media_type", "video");

        assertFalse(NasaCommandHandler.isImage(json));
    }
}
