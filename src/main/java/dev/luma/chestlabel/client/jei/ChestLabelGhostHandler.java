package dev.luma.chestlabel.client.jei;

import dev.luma.chestlabel.client.ChestScreenHandler;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChestLabelGhostHandler implements IGhostIngredientHandler<AbstractContainerScreen<?>> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(AbstractContainerScreen<?> gui, ITypedIngredient<I> ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();

        if (!ChestScreenHandler.isLogoSlotActive()) {
            return targets;
        }

        if (!(ingredient.getIngredient() instanceof ItemStack)) {
            return targets;
        }

        Rect2i area = new Rect2i(ChestScreenHandler.getLogoSlotX(), ChestScreenHandler.getLogoSlotY(), 18, 18);

        targets.add(new Target<I>() {
            @Override
            public Rect2i getArea() {
                return area;
            }

            @Override
            public void onComplete() {
                Object raw = ingredient.getIngredient();
                if (raw instanceof ItemStack stack) {
                    ChestScreenHandler.applyGhostItem(stack);
                }
            }
        });

        return targets;
    }

    @Override
    public void onComplete() {
    }
}