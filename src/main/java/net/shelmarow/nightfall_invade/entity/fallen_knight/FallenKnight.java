package net.shelmarow.nightfall_invade.entity.fallen_knight;

import com.hm.efn.registries.EFNItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.item.EpicFightItems;

public class FallenKnight extends PathfinderMob {

    public FallenKnight(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 100;
        setEquipment();
    }

    private void setEquipment() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(EpicFightItems.IRON_GREATSWORD.get()));
        this.setDropChance(EquipmentSlot.MAINHAND,0F);
        this.setDropChance(EquipmentSlot.OFFHAND,0F);
        this.setDropChance(EquipmentSlot.HEAD,0F);
        this.setDropChance(EquipmentSlot.CHEST,0F);
        this.setDropChance(EquipmentSlot.LEGS,0F);
        this.setDropChance(EquipmentSlot.FEET,0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)

                .add(EpicFightAttributes.IMPACT.get(),0.5D)
                .add(EpicFightAttributes.ARMOR_NEGATION.get(),30D)
                .add(EpicFightAttributes.STUN_ARMOR.get(),8.0D)
                .add(EpicFightAttributes.MAX_STRIKES.get(),10.0D)
                .add(EpicFightAttributes.MAX_STAMINA.get(),30.0D)
                .add(EpicFightAttributes.STAMINA_REGEN.get(),1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class,12));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }
}
