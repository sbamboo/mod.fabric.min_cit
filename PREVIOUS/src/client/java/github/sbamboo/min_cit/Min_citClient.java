package github.sbamboo.min_cit;

import github.sbamboo.min_cit.resource.CitResourceReloader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class Min_citClient implements ClientModInitializer {
	public static final String MOD_ID = "min_cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		// Register our resource reloader
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
			.registerReloadListener(
					new SimpleSynchronousResourceReloadListener() {
						@Override
						public Identifier getFabricId() {
							return Identifier.of(MOD_ID, "cit");
						}

						@Override
						public void reload(ResourceManager manager) {
							LOGGER.info( manager.getAllNamespaces().toString() );
							LOGGER.info(
								manager.getAllResources( Identifier.of(MOD_ID, "cit") )
									.toString()
							);
						}
					}
			);

		LOGGER.info("Client initialization completed!");
	}
}