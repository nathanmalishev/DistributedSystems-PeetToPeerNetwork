package activitystreamer.messages;

/* Based off implementation provided here: http://stackoverflow.com/questions/21626690/gson-optional-and-required-fields */

import com.google.gson.*;
import java.lang.reflect.*;
import org.apache.logging.log4j.Logger;
import java.util.*;

class EnforcedDeserializer<JsonMessage> implements JsonDeserializer<JsonMessage>{

        private Logger log;

        public EnforcedDeserializer(Logger log) {
            this.log = log;
        }

        public JsonMessage deserialize(JsonElement msg, Type type, JsonDeserializationContext jdc) throws JsonParseException{
            System.out.println("MEssafe is: " + msg);
            Gson gson = new Gson();
            JsonMessage newMsg = gson.fromJson(msg, type);

            Field[] attributes = newMsg.getClass().getDeclaredFields();

            ArrayList<String> attributeNames = new ArrayList<String>();

            for (Field f : attributes) {
                attributeNames.add(f.getName());
            }
            System.out.println(attributeNames);
            attributeNames.add("command");
            attributeNames.remove("version");

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

            attributeNames.add("version");
            for ( Map.Entry<String, JsonElement> entry : msg.getAsJsonObject().entrySet() ) {
                if (!attributeNames.contains(entry.getKey())) {
                    throw new JsonParseException("Extra field");
                }

            }
            return newMsg;
        }

}
