package com.chaosthedude.naturescompass.gui;

import java.util.Objects;

import com.chaosthedude.naturescompass.util.RenderUtils;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeSearchList extends ExtendedList<BiomeSearchEntry> {

	private final NaturesCompassScreen guiNaturesCompass;

	public BiomeSearchList(NaturesCompassScreen guiNaturesCompass, Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
		super(mc, width, height, top, bottom, slotHeight);
		this.guiNaturesCompass = guiNaturesCompass;
		refreshList();
	}

	@Override
	protected int func_230952_d_() {
		return super.func_230952_d_() + 20;
	}

	@Override
	public int func_230949_c_() {
		return super.func_230949_c_() + 50;
	}

	@Override
	protected boolean func_230957_f_(int slotIndex) {
		return slotIndex >= 0 && slotIndex < func_231039_at__().size() ? func_231039_at__().get(slotIndex).equals(func_230958_g_()) : false;
	}

	@Override
	public void func_230430_a_(MatrixStack matrixStack, int par1, int par2, float par3) {
		int i = func_230952_d_();
		int k = func_230968_n_();
		int l = field_230672_i_ + 4 - (int) func_230966_l_();

		func_238478_a_(matrixStack, k, l, par1, par2, par3);
	}

	@Override
	protected void func_238478_a_(MatrixStack matrixStack, int par1, int par2, int par3, int par4, float par5) {
		int i = func_230965_k_();
		for (int j = 0; j < i; ++j) {
			int k = func_230962_i_(j);
			int l = getRowBottom(j);
			if (l >= field_230672_i_ && k <= field_230673_j_) {
				int j1 = this.field_230669_c_ - 4;
				BiomeSearchEntry e = this.func_230953_d_(j);
				int k1 = func_230949_c_();
				if (/*renderSelection*/ true && func_230957_f_(j)) {
					final int insideLeft = field_230675_l_ + field_230670_d_ / 2 - func_230949_c_() / 2 + 2;
					RenderUtils.drawRect(insideLeft - 4, k - 4, insideLeft + func_230949_c_() + 4, k + field_230669_c_, 255 / 2 << 24);
				}

				int j2 = func_230968_n_();
				e.func_230432_a_(matrixStack, j, k, j2, k1, j1, par3, par4, func_231047_b_((double) par3, (double) par4) && Objects .equals(func_230933_a_((double) par3, (double) par4), e), par5);
			}
		}

	}

	private int getRowBottom(int p_getRowBottom_1_) {
		return this.func_230962_i_(p_getRowBottom_1_) + this.field_230669_c_;
	}

	public void refreshList() {
		func_230963_j_();
		for (Biome biome : guiNaturesCompass.sortBiomes()) {
			func_230513_b_(new BiomeSearchEntry(this, biome));
		}
		selectBiome(null);
	}

	public void selectBiome(BiomeSearchEntry entry) {
		func_241215_a_(entry);
		guiNaturesCompass.selectBiome(entry);
	}

	public boolean hasSelection() {
		return func_230958_g_() != null;
	}

	public NaturesCompassScreen getGuiNaturesCompass() {
		return guiNaturesCompass;
	}

}
