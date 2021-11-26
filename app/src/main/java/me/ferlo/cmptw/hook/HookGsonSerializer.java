package me.ferlo.cmptw.hook;

import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static me.ferlo.cmptw.hook.FileBasedHookService.UTF_8_BOM;

class HookGsonSerializer implements JsonSerializer<Hook>, JsonDeserializer<Hook> {

    public static final Gson SERIALIZER = new GsonBuilder()
            .registerTypeAdapter(Hook.class, new HookGsonSerializer())
            .registerTypeAdapter(Hook.Device.class, new DeviceSerializer())
            .registerTypeAdapter(Hook.ApplicationHook.class, new ApplicationHookSerializer())
            .registerTypeAdapter(Hook.Application.class, new ApplicationSerializer())
            .registerTypeAdapter(Hook.HookScript.class, new HookScriptSerializer())
            .registerTypeAdapter(Hook.KeyStroke.class, new KeyStrokeSerializer())
            .create();

    @Override
    public JsonElement serialize(Hook src, Type typeOfSrc, JsonSerializationContext ctx) {
        final JsonObject hook = new JsonObject();
        hook.add("device", ctx.serialize(src.device()));

        final JsonArray applicationHooks = new JsonArray();
        src.applicationHooks().forEach(appHook -> applicationHooks.add(ctx.serialize(appHook)));
        hook.add("application_hooks", applicationHooks);

        hook.add("fallback_behavior", ctx.serialize(src.fallbackBehavior()));
        return hook;
    }

    @Override
    public Hook deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
        final JsonObject obj = json.getAsJsonObject();
        return new Hook(
                ctx.deserialize(obj.get("device"), Hook.Device.class),
                StreamSupport.stream(obj.getAsJsonArray("application_hooks").spliterator(), false)
                        .map(element -> (Hook.ApplicationHook) ctx.deserialize(element, Hook.ApplicationHook.class))
                        .collect(Collectors.toList()),
                ctx.deserialize(obj.get("fallback_behavior"), Hook.FallbackBehavior.class));
    }

    protected static class DeviceSerializer implements JsonSerializer<Hook.Device>, JsonDeserializer<Hook.Device> {

        @Override
        public JsonElement serialize(Hook.Device src, Type typeOfSrc, JsonSerializationContext ctx) {
            final JsonObject device = new JsonObject();
            device.add("id", new JsonPrimitive(src.id()));
            device.add("name", new JsonPrimitive(src.name()));
            device.add("desc", new JsonPrimitive(src.desc()));
            return device;
        }

        @Override
        public Hook.Device deserialize(JsonElement json,
                                       Type typeOfT,
                                       JsonDeserializationContext ctx) throws JsonParseException {
            final JsonObject obj = json.getAsJsonObject();
            return new Hook.Device(
                    obj.getAsJsonPrimitive("id").getAsString(),
                    obj.getAsJsonPrimitive("name").getAsString(),
                    obj.getAsJsonPrimitive("desc").getAsString());
        }
    }

    protected static class ApplicationHookSerializer implements JsonSerializer<Hook.ApplicationHook>, JsonDeserializer<Hook.ApplicationHook> {

        @Override
        public JsonElement serialize(Hook.ApplicationHook src, Type typeOfSrc, JsonSerializationContext ctx) {
            final JsonObject appHook = new JsonObject();
            appHook.add("application", ctx.serialize(src.application()));

            final JsonArray scripts = new JsonArray();
            src.scripts().forEach(script -> scripts.add(ctx.serialize(script)));
            appHook.add("scripts", scripts);

            appHook.add("folder", new JsonPrimitive(src.folder().toAbsolutePath().toString()));
            return appHook;
        }

        @Override
        public Hook.ApplicationHook deserialize(JsonElement json,
                                                Type typeOfT,
                                                JsonDeserializationContext ctx) throws JsonParseException {
            final JsonObject obj = json.getAsJsonObject();
            return new Hook.ApplicationHook(
                    ctx.deserialize(obj.get("application"), Hook.Application.class),
                    StreamSupport.stream(obj.getAsJsonArray("scripts").spliterator(), false)
                            .map(element -> (Hook.HookScript) ctx.deserialize(element, Hook.HookScript.class))
                            .collect(Collectors.toList()),
                    Paths.get(obj.getAsJsonPrimitive("folder").getAsString()));
        }
    }

    protected static class ApplicationSerializer implements JsonSerializer<Hook.Application>, JsonDeserializer<Hook.Application> {

        @Override
        public JsonElement serialize(Hook.Application src, Type typeOfSrc, JsonSerializationContext ctx) {
            final JsonObject application = new JsonObject();
            application.add("process", new JsonPrimitive(src.process()));
            application.add("name", new JsonPrimitive(src.name()));
            application.add("icon", new JsonPrimitive(src.icon().toAbsolutePath().toString()));
            return application;
        }

        @Override
        public Hook.Application deserialize(JsonElement json,
                                            Type typeOfT,
                                            JsonDeserializationContext ctx) throws JsonParseException {
            final JsonObject obj = json.getAsJsonObject();
            return new Hook.Application(
                    obj.getAsJsonPrimitive("process").getAsString(),
                    obj.getAsJsonPrimitive("name").getAsString(),
                    Paths.get(obj.getAsJsonPrimitive("icon").getAsString()));
        }
    }

    protected static class HookScriptSerializer implements JsonSerializer<Hook.HookScript>, JsonDeserializer<Hook.HookScript> {

        @Override
        public JsonElement serialize(Hook.HookScript src, Type typeOfSrc, JsonSerializationContext ctx) {
            final JsonObject script = new JsonObject();
            script.add("name", new JsonPrimitive(src.name()));
            script.add("key_stroke", ctx.serialize(src.keyStroke()));
            script.add("script", new JsonPrimitive(src.scriptFile().toAbsolutePath().toString()));
            return script;
        }

        @Override
        public Hook.HookScript deserialize(JsonElement json,
                                           Type typeOfT,
                                           JsonDeserializationContext ctx) throws JsonParseException {
            final JsonObject obj = json.getAsJsonObject();
            final Path scriptFile = Paths.get(obj.getAsJsonPrimitive("script").getAsString());

            String scriptContent;
            try {
                scriptContent = Files.readString(scriptFile);
                // Remove the BOM if present
                if(scriptContent.startsWith(UTF_8_BOM))
                    scriptContent = scriptContent.substring(UTF_8_BOM.length());
            } catch (IOException ex) {
                throw new JsonIOException("Failed to read script file content from " + scriptFile.toAbsolutePath(), ex);
            }

            return new Hook.HookScript(
                    obj.getAsJsonPrimitive("name").getAsString(),
                    ctx.deserialize(obj.get("key_stroke"), Hook.KeyStroke.class),
                    scriptContent,
                    scriptFile);
        }
    }

    protected static class KeyStrokeSerializer implements JsonSerializer<Hook.KeyStroke>, JsonDeserializer<Hook.KeyStroke> {

        @Override
        public JsonElement serialize(Hook.KeyStroke src, Type typeOfSrc, JsonSerializationContext ctx) {
            final JsonObject keyStroke = new JsonObject();
            // TODO: save as plain text?
            keyStroke.add("key_code", new JsonPrimitive(src.keyCode()));
            keyStroke.add("modifiers", new JsonPrimitive(src.modifiers()));
            keyStroke.add("toggle_modifiers_mask", new JsonPrimitive(src.toggleModifiersMask()));
            return keyStroke;
        }

        @Override
        public Hook.KeyStroke deserialize(JsonElement json,
                                          Type typeOfT,
                                          JsonDeserializationContext ctx) throws JsonParseException {
            final JsonObject obj = json.getAsJsonObject();
            return new Hook.KeyStroke(
                    obj.getAsJsonPrimitive("key_code").getAsInt(),
                    obj.getAsJsonPrimitive("modifiers").getAsInt(),
                    obj.getAsJsonPrimitive("toggle_modifiers_mask").getAsInt());
        }
    }
}
