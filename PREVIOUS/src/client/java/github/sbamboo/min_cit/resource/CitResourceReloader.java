package github.sbamboo.min_cit.resource;

import github.sbamboo.min_cit.Min_citClient;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CitResourceReloader implements IdentifiableResourceReloadListener {
    @Override
    public CompletableFuture<Void> reload(
            Synchronizer synchronizer,
            ResourceManager manager,
            Executor prepareExecutor,
            Executor applyExecutor
    ) {
        // Prepare stage - find all .properties files
        CompletableFuture<Void> prepareStage = CompletableFuture.supplyAsync(() -> {
            Min_citClient.LOGGER.info( manager.getAllNamespaces().toString() );
            Identifier mod_identifier = Identifier.of("min_cit","textures/item");
            Min_citClient.LOGGER.info( manager.getAllResources(mod_identifier).toString() );
            return null;
        }, prepareExecutor);

        // Move to apply stage through synchronizer
        CompletableFuture<Void> applyStage = prepareStage.thenCompose(synchronizer::whenPrepared);

        // Apply stage - process the properties files
        return applyStage.thenRunAsync(() -> {
            Min_citClient.LOGGER.info("Applying CIT resources");
        }, applyExecutor);
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(Min_citClient.MOD_ID);
    }
}