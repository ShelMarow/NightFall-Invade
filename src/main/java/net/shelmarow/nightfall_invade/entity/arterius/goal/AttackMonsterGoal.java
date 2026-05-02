package net.shelmarow.nightfall_invade.entity.arterius.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class AttackMonsterGoal extends TargetGoal {
    @Nullable
    protected LivingEntity target;
    protected TargetingConditions targetConditions;

    public AttackMonsterGoal(Mob pMob, boolean pMustSee) {
        this(pMob, pMustSee,false);
    }

    public AttackMonsterGoal(Mob pMob, boolean pMustSee, boolean pMustReach) {
        this(pMob, pMustSee, pMustReach,null);
    }


    public AttackMonsterGoal(Mob pMob, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pTargetPredicate) {
        super(pMob, pMustSee, pMustReach);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(pTargetPredicate);
    }

    @Override
    public boolean canUse() {
        findTarget();
        return target != null;
    }

    private void findTarget() {
        this.target = this.mob.level().getNearestEntity(
                this.mob.level().getEntitiesOfClass(Mob.class, getTargetSearchArea(getFollowDistance())).stream()
                .filter(pEntity -> pEntity != this.mob && pEntity.getType().getCategory() == MobCategory.MONSTER && this.mob.hasLineOfSight(pEntity)).toList(),
                targetConditions,this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()
        );
    }


    private AABB getTargetSearchArea(double pTargetDistance) {
        return this.mob.getBoundingBox().inflate(pTargetDistance, 4.0D, pTargetDistance);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable LivingEntity pTarget) {
        this.target = pTarget;
    }
}
