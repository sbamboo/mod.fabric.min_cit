package github.sbamboo.min_cit;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
            return null;
        }, prepareExecutor);

        // Move to apply stage through synchronizer
        CompletableFuture<Void> applyStage = prepareStage.thenCompose(synchronizer::whenPrepared);

        // Apply stage - process the properties files
        return applyStage.thenRunAsync(() -> {
            Min_cit.LOGGER.info("Applying CIT resources");
        }, applyExecutor);
    }
}
