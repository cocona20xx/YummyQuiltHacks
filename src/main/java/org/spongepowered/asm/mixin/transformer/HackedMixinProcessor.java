package org.spongepowered.asm.mixin.transformer;

import net.auoeke.reflect.Accessor;
import net.cursedmc.yqh.api.mixin.Mixout;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IHotSwap;

public class HackedMixinProcessor extends MixinProcessor {
	HackedMixinProcessor(MixinEnvironment environment, Extensions extensions, IHotSwap hotSwapper, MixinCoprocessorNestHost nestHostCoprocessor) {
		super(environment, extensions, hotSwapper, nestHostCoprocessor);
	}
	
	@Override
	synchronized boolean applyMixins(MixinEnvironment environment, String name, ClassNode targetClassNode) {
		boolean shouldApply;
		
		shouldApply = Mixout.TransformEvent.preMixin(name, targetClassNode);
		shouldApply |= super.applyMixins(environment, name, targetClassNode);
		shouldApply |= Mixout.TransformEvent.postMixin(name, targetClassNode);
		
		return shouldApply;
	}
}
