/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

/**
 * Paper plugin loader for ASSASIN.
 *
 * <p>Declares runtime library dependencies that Paper will resolve and add to
 * the plugin classloader before the plugin is instantiated. This is the
 * recommended approach for Paper plugins that shade heavy dependencies.
 *
 * <p>Note: PacketEvents, HikariCP, and Caffeine are shaded via shadowJar and
 * do not need to be declared here. This loader is kept as a hook for any
 * future runtime-resolved libraries.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class AssasinLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        // All heavy dependencies (PacketEvents, HikariCP, Caffeine) are shaded
        // into the JAR via shadowJar — no runtime resolution needed here.
        //
        // If future phases require runtime-resolved libraries (e.g., ONNX runtime
        // for ML macro detection in v2.0), add them here using MavenLibraryResolver.
        //
        // Example:
        //   MavenLibraryResolver resolver = new MavenLibraryResolver();
        //   resolver.addRepository(new RemoteRepository.Builder(
        //       "central", "default", "https://repo.maven.apache.org/maven2/").build());
        //   resolver.addDependency(new Dependency(
        //       new DefaultArtifact("some.group:artifact:1.0.0"), null));
        //   classpathBuilder.addLibrary(resolver);
    }
}
