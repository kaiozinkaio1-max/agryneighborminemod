package angryneighbor;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class NeighborEntity extends PathAwareEntity {
    public NeighborEntity(EntityType<? extends NeighborEntity> type, World world) { super(type, world); }

    public static DefaultAttributeContainer.Builder attrs() {
        return MobEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 60).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.33)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40);
    }

    @Override protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new MeleeAttackGoal(this, 1.15, false));
        goalSelector.add(2, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 12f));
        targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    /** 0 = facil, 1 = normal, 2 = dificil */
    public void applyMode(int mode) {
        double[] speed = {0.27, 0.33, 0.42}, dmg = {2, 4, 7};
        getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(speed[mode]);
        getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(dmg[mode]);
        setHealth(getMaxHealth());
    }
}
