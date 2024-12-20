package github.sbamboo.min_cit;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Min_cit implements ModInitializer {
	public static final String MOD_ID = "min_cit";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Map<String, Properties> LoadedProperties = new HashMap<>();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// Get the mod container for this mod ID
		ModContainer modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElse(null);

		// Retrieve the version from the mod metadata or set to "unknown"
		String version = (modContainer != null) ? modContainer.getMetadata().getVersion().getFriendlyString() : "unknown";

		// Retrieve authors and join them with "&"
		String authors = (modContainer != null) ? modContainer.getMetadata().getAuthors().stream()
				.map(Person::getName)
				.collect(Collectors.joining("&")) : "unknown";
		authors = capitalizeFirstLetter(authors);

		LOGGER.info(authors + "'s MinCIT " + version + " loaded!");

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
			new CitAsyncResourceLoader()
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

	// Method to capitalize the first letter of a string
	private String capitalizeFirstLetter(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}


	public static void addLoadedProperty(String key, Properties properties) {
		LoadedProperties.put(key, properties);
	}

	public static void removeLoadedProperty(String key) {
		LoadedProperties.remove(key);
	}

	public static boolean hasLoadedProperty(String key) {
		return LoadedProperties.containsKey(key);
	}

	public static void clearLoadedProperties() {
		LoadedProperties.clear();
	}

	public static void debugLoadedProperties(Logger ActiveLogger) {
		for (Map.Entry<String, Properties> entry : LoadedProperties.entrySet()) {
			String stringKey = entry.getKey();
			Properties properties = entry.getValue();

			ActiveLogger.info(stringKey + ":");
			for (String propertyKey : properties.stringPropertyNames()) {
				String propertyValue = properties.getProperty(propertyKey);
				ActiveLogger.info("    " + propertyKey + ": " + propertyValue);
			}
		}
	}

	public static String getFileName(String input) {
		String filePath;

		// Step 1: Check for ":"
		if (input.contains(":")) {
			// Step 2: Split by ":"
			filePath = input.split(":")[1];
		} else {
			filePath = input;
		}

		// Step 3: Extract filename without extension
		// Find the last "/" if any, to isolate the filename
		String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

		// Remove the extension if present
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0) { // Ensure there is a dot and it's not the first character
			fileName = fileName.substring(0, dotIndex);
		}

		return fileName;
	}
}