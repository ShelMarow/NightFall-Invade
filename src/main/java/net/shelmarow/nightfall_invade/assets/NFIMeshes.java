package net.shelmarow.nightfall_invade.assets;

import net.shelmarow.nightfall_invade.NightFallInvade;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SkinnedMesh;

public class NFIMeshes {

    public static Meshes.MeshAccessor<SkinnedMesh> BLOOD_SLASH_MESH =
            Meshes.MeshAccessor.create(NightFallInvade.MOD_ID,"entity/blood_slash",
                    (jsonModelLoader) -> jsonModelLoader.loadSkinnedMesh(SkinnedMesh::new));
}
