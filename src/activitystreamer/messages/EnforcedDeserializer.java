package activitystreamer.messages;

/* Based off implementation provided here: http://stackoverflow.com/questions/21626690/gson-optional-and-required-fields */

import com.google.gson.*;
import java.lang.reflect.*;
import org.apache.logging.log4j.Logger;

class EnforcedDeserializer<JsonMessage> implements JsonDeserializer<JsonMessage>{

        private Logger log;

        public EnforcedDeserializer(Logger log) {
            this.log = log;
        }

        public JsonMessage deserialize(JsonElement msg, Type type, JsonDeserializationContext jdc) throws JsonParseException{

            Gson gson = new Gson();
            JsonMessage newMsg = gson.fromJson(msg, type);

            Field[] attributes = newMsg.getClass().getDeclaredFields();

            for (Field f : attributes) {

                try {
                    f.setAccessible(true);
                    if (f.get(newMsg) == null) {
                        throw new JsonParseException("Missing field");
                    }
                } catch (IllegalArgumentException ex) {
                    log.error(ex);
                } catch (IllegalAccessException ex) {
                    log.error(ex);
                }
            }




            return newMsg;

        }

}
