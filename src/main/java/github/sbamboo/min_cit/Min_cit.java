package github.sbamboo.min_cit;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class Min_cit implements ModInitializer {
	public static final String MOD_ID = "min_cit";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
			new SimpleSynchronousResourceReloadListener() {
				@Override
				public Identifier getFabricId() {
					return Identifier.ofVanilla("optifine/cit");
				}

				@Override
				public void reload(ResourceManager manager) {
					LOGGER.info( manager.getAllNamespaces().toString() );

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

							LOGGER.info(String.valueOf(properties));

							stream.close();
						} catch (Exception e) {
							LOGGER.error("Error occurred while loading cit properties" + entry.getKey().toString(), e);
						}
					}
				}
			}
		);
	}

	public static String resolveIpattern(String input) {
		// If the string is in the literal form `<literalstring>`
		if (!input.startsWith("ipattern:")) {
			// Just return the input as it is, since it's an exact match
			return "^" + Pattern.quote(input) + "$";
		}

		// Remove the `ipattern:` prefix
		String patternContent = input.substring(9);

		// Check for different cases
		if (patternContent.startsWith("*") && patternContent.endsWith("*")) {
			// ipattern:*<string>*: Match contains <string>
			String substring = patternContent.substring(1, patternContent.length() - 1);
			return ".*" + Pattern.quote(substring) + ".*";
		} else if (patternContent.startsWith("*")) {
			// ipattern:*<string>: Match ends with <string>
			String substring = patternContent.substring(1);
			return ".*" + Pattern.quote(substring) + "$";
		} else if (patternContent.endsWith("*")) {
			// ipattern:<string>*: Match begins with <string>
			String substring = patternContent.substring(0, patternContent.length() - 1);
			return "^" + Pattern.quote(substring) + ".*";
		} else {
			// ipattern:<regex>: Treat it as a raw regex pattern
			return patternContent;
		}
	}
}