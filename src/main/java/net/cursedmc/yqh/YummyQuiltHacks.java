package net.cursedmc.yqh;

import net.auoeke.reflect.ClassDefiner;
import net.auoeke.reflect.Classes;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.cursedmc.yqh.api.instrumentation.Music;
import net.cursedmc.yqh.api.mixin.Mixout;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.LanguageAdapter;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.plugin.ModMetadataExt;
import org.quiltmc.loader.impl.launch.knot.Knot;
import org.quiltmc.loader.impl.metadata.qmj.AdapterLoadableClassEntry;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.jar.JarFile;

import static com.enderzombi102.enderlib.SafeUtils.doSafely;

public class YummyQuiltHacks implements LanguageAdapter {
	public static final ClassLoader UNSAFE_LOADER;

	@Override
	public native <T> T create(ModContainer mod, String value, Class<T> type);

	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks");

	static {
		ClassLoader appLoader = Knot.class.getClassLoader();
		ClassLoader knotLoader = YummyQuiltHacks.class.getClassLoader();

		String jarPath = Objects.requireNonNull(YummyQuiltHacks.class.getClassLoader().getResource("yummy_agent.jar")).getPath();
		String vmPid = String.valueOf(ManagementFactory.getRuntimeMXBean().getPid());

		if (jarPath.startsWith("file:")) {
			// sanitize path
			jarPath = jarPath.replaceAll("file:|!/yummy_agent\\.jar", "");
			jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);

			try {
				// find yummy_agent.jar inside jar and make a temp jar of it
				JarFile jar = new JarFile(FileUtils.getFile(jarPath));
				byte[] jarBytes = jar.getInputStream(jar.getJarEntry("yummy_agent.jar")).readAllBytes();
				jar.close();
				File tempJar = File.createTempFile("tmp_", null);
				FileUtils.writeByteArrayToFile(tempJar, jarBytes);
				ByteBuddyAgent.attach(FileUtils.getFile(tempJar.getAbsolutePath()), vmPid);
			} catch (Exception e){
				throw new RuntimeException(e);
			}
		} else {
			if (SystemUtils.IS_OS_WINDOWS) {
				jarPath = jarPath.replaceFirst("/", ""); // windows bad
			}

			ByteBuddyAgent.attach(FileUtils.getFile(jarPath), vmPid);
		}

		final String[] manualLoad = {
				"net.gudenau.lib.unsafe.Unsafe",
				"net.devtech.grossfabrichacks.unsafe.UnsafeUtil",
				"net.devtech.grossfabrichacks.unsafe.UnsafeUtil$FirstInt",
		};

		for (String name : manualLoad) {
			if (name.equals("net.gudenau.lib.unsafe.Unsafe") && QuiltLoader.isDevelopmentEnvironment()) {
				continue;
			}

			byte[] classBytes = Classes.classFile(knotLoader, name);

			if (classBytes == null) {
				LOGGER.warn("Could not find class bytes for class " + name + " in loader " + knotLoader);
				continue;
			}

			ClassDefiner.make()
					.name(name)
					.classFile(classBytes)
					.loader(appLoader)
					.protectionDomain(YummyQuiltHacks.class.getProtectionDomain())
					.define();
		}

		UNSAFE_LOADER = UnsafeUtil.defineAndInitializeAndUnsafeCast(knotLoader, "org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader", appLoader);

		UnsafeUtil.initializeClass(Music.class);
		UnsafeUtil.initializeClass(Mixout.class);

		LOGGER.fatal("Quilt has been successfully pwned >:3");

		for(ModContainer mod : QuiltLoader.getAllMods()){
			ModMetadataExt ext = (ModMetadataExt) mod.metadata(); //no clue if this works!!!
			if(ext.getEntrypoints().containsKey("yqh:pre_mixin")) {
				for(AdapterLoadableClassEntry entry : ext.getEntrypoints().get("yqh:pre_mixin")) {
					Class<?> preMixinClass = ClassDefiner.make()
						.classFile(entry.getValue())
						.name(entry.getValue())
						.loader(appLoader)
						.define();
					Object preMixin = doSafely(() -> preMixinClass.getConstructor().newInstance());
					doSafely(() -> preMixin.getClass().getMethod("onPreMixin").invoke(preMixin));
				}
			}
		}
	}
}
