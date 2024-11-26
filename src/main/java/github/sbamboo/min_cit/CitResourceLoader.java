package github.sbamboo.min_cit;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class CitResourceLoader implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Identifier.ofVanilla("optifine/cit");
    }

    @Override
    public void reload(ResourceManager manager) {
        Min_cit.LOGGER.info( manager.getAllNamespaces().toString() );

        // https://optifine.readthedocs.io/cit.html

        for(Map.Entry<Identifier, Resource> entry : manager.findResources("optifine/cit", path -> path.toString().endsWith(".properties")).entrySet() ) {
            try(InputStream stream = entry.getValue().getInputStream()) {
                // Consume
                Properties properties = new Properties();
                properties.load(stream);

                // Handle matchItems
                String matchItems = "";
                if (!properties.containsKey("matchItems")) {
                    matchItems = properties.getProperty("items");
                    properties.remove("items");
                } else {
                    matchItems = properties.getProperty("matchItems");
                }
                String[] parts = matchItems.split(" ");
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].contains(":")) {
                        parts[i] = "minecraft:" + parts[i];  // Add prefix if no colon
                    }
                }
                properties.setProperty("matchItems",String.join(" ", parts));

                // Handle nbt.display.Name
                String display_name = properties.getProperty("nbt.display.Name");
                properties.remove("nbt.display.Name");
                properties.setProperty("display_name_pattern", Min_cit.resolveIpattern(display_name) );

                Min_cit.LOGGER.info(String.valueOf(properties));

                stream.close();
            } catch (Exception e) {
                Min_cit.LOGGER.error("Error occurred while loading cit properties" + entry.getKey().toString(), e);
            }
        }
    }
}

