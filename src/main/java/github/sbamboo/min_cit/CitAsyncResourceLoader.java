package github.sbamboo.min_cit;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class CitAsyncResourceLoader implements IdentifiableResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Identifier.ofVanilla("optifine/cit");
    }

    @Override
    public CompletableFuture<Void> reload(
            Synchronizer synchronizer,
            ResourceManager manager,
            Executor prepareExecutor,
            Executor applyExecutor
    ) {
        // Prepare stage - find all .properties files
        CompletableFuture<Void> prepareStage = CompletableFuture.supplyAsync(() -> {
            Min_cit.LOGGER.info( manager.getAllNamespaces().toString() );

            for(Map.Entry<Identifier, Resource> entry : manager.findResources("optifine/cit", path -> path.toString().endsWith(".properties")).entrySet() ) {
                try(InputStream stream = entry.getValue().getInputStream()) {
                    // Consume
                    Properties properties = new Properties();
                    properties.load(stream);

                    // Handle matchItems
                    String matchItems = "";
                    if (!properties.containsKey("matchItems")) {
                        if (properties.containsKey("items")) {
                            matchItems = properties.getProperty("items");
                            properties.remove("items");
                        }
                    } else {
                        matchItems = properties.getProperty("matchItems");
                    }
                    String[] parts = matchItems.split(" ");
                    for (int i = 0; i < parts.length; i++) {
                        // If non-empty string, add minecraft: namespace if no namespace is supplied
                        if (!parts[i].replaceAll("\\s", "").isEmpty()) {
                            if (!parts[i].contains(":")) {
                                parts[i] = "minecraft:" + parts[i];  // Add prefix if no colon
                            }
                        }
                    }
                    properties.setProperty("matchItems",String.join(" ", parts));

                    if (properties.getProperty("matchItems").replaceAll("\\s", "").isEmpty()) {
                        properties.setProperty("matchItems", "minecraft:" + Min_cit.getFileName(entry.getKey().toString()));
                    }

                    // Handle nbt.display.Name
                    String display_name = "";
                    if (properties.containsKey("nbt.display.Name")) {
                        display_name = properties.getProperty("nbt.display.Name");
                        properties.remove("nbt.display.Name");
                    }
                    properties.setProperty("display_name_pattern", Min_cit.resolveIpattern(display_name) );

                    // Handle texture
                    //// Temporary map to hold the extracted texture properties
                    Map<String, String> textureMap = new HashMap<>();
                    if (properties.containsKey("texture")) {
                        textureMap.put("layer0",properties.getProperty("texture"));
                        properties.remove("texture");
                    }

                    //// Collect keys starting with "texture."
                    List<String> textureKeys = properties.stringPropertyNames().stream()
                            .filter(key -> key.startsWith("texture."))
                            .toList();

                    //// Process each key
                    textureKeys.forEach(key -> {
                        String newKey = key.substring("texture.".length());
                        textureMap.put(newKey, properties.getProperty(key));
                        properties.remove(key); // Remove the key from Properties
                    });

                    //// Add the map as a serialized string under "textures" key
                    String texturesSerialized = textureMap.entrySet().stream()
                            .map(toSerializeEntry -> toSerializeEntry.getKey() + "=" + toSerializeEntry.getValue())
                            .collect(Collectors.joining(","));
                    properties.setProperty("textures", texturesSerialized);

                    // Add to main class
                    Min_cit.addLoadedProperty( entry.getKey().toString(), properties );

                    stream.close();
                } catch (Exception e) {
                    Min_cit.LOGGER.error("Error occurred while loading cit properties" + entry.getKey().toString(), e);
                }
            }

            return null;
        }, prepareExecutor);

        // Move to apply stage through synchronizer
        CompletableFuture<Void> applyStage = prepareStage.thenCompose(synchronizer::whenPrepared);

        // Apply stage - process the properties files
        return applyStage.thenRunAsync(() -> {
            Min_cit.LOGGER.info("Applying CIT resources");
            Min_cit.debugLoadedProperties(Min_cit.LOGGER);
        }, applyExecutor);
    }
}
