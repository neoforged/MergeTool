/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.neoforged.api.distmarker;

/**
 * A physical distribution of Minecraft. There are two common distributions, and though
 * much code is common between them, there are some specific pieces that are only present
 * in one or the other.
 * <ul>
 *     <li>{@link #CLIENT} is the <em>client</em> distribution, it contains
 *     the game client, and has code to render a viewport into a game world.</li>
 *     <li>{@link #DEDICATED_SERVER} is the <em>dedicated server</em> distribution,
 *     it contains a server, which can simulate the world and communicates via network.</li>
 * </ul>
 * <p>
 * Code that is only present in a specific dist is referred to as "dist-specific code", 
 * and will be marked with {@link OnlyIn}. Code that is always available is referred to 
 * as "shared code" (or "common code"). It is also common to refer to dist-specific code 
 * as "client-only code" or "server-only code" to indicate the dist.
 * <p>
 * To prevent classloading errors, it is important to ensure that {@link OnlyIn} elements are 
 * only loaded if their designated dist is the same as the executing dist.
 * <p>
 * This is done by obeying the following rules:
 * <ol>
 * <li>All dist-specific code must go in a separate class, called a "bouncer" class.</li>
 * <li>All accesses to the bouncer class must be guarded by a dist check.</li>
 * </ol>
 * <p>
 * An example of these rules in action is shown below:
 * <p>
 * <pre>{@code
 * // Client-only bouncer class which accesses client-only code. 
 * // Methods in this class will fail verification if invoked on the wrong dist.
 * // However, the class can still be referenced from shared code as long as no methods are invoked unconditionally.
 * public class ClientBouncer
 * {
 *     public static boolean isClientSingleplayer()
 *     {
 *         // Minecraft is @OnlyIn(Dist.CLIENT)
 *         return Minecraft.getInstance().isSingleplayer();
 *     }
 * }
 * 
 * // Class which may be loaded on either dist.
 * public class SharedClass 
 * {
 *     // Returns true if the client is playing singleplayer.
 *     // Returns false if executed on the server. Will never crash.
 *     public static boolean isClientSingleplayer()
 *     {
 *         if(currentDist.isClient())
 *         {
 *             return ClientBouncer.isClientSingleplayer();
 *         }
 *         return false;
 *     }
 * }
 * }</pre>
 * 
 * In this example, any code can now call {@code SharedClass.isClientSingleplayer()} without guarding.
 * <p>
 * The specifics of why this works relies on how the class verifier operates. When the shared method 
 * is invoked for the first time, the following steps occur:
 * <ol>
 * <li>The class {@code SharedClass} is loaded, if it was not previously accessed</li>
 * <li>The method {@code SharedClass.isClientSingleplayer} is loaded and verified.</li>
 * <li>It is checked that {@code ClientBouncer} exists, but the content of its methods are not yet verified.</li>
 * <li>If running on {@link Dist#CLIENT}, then {@code ClientBouncer.isClientSingleplayer} will be verified.</li>
 * </ol>
 * The final step causes the verifier to resolve a reference to {@code Minecraft}, which is client-only code.
 * If this step happend on {@link Dist#DEDICATED_SERVER} (i.e. if the dist check were omitted), the game would crash.
 * 
 * @apiNote How to access the current Dist will depend on the project. When using FML, it is in FMLEnvironment.dist
 */
public enum Dist {

    /**
     * The client distribution. This is the game client players can purchase and play.
     * It contains the graphics and other rendering to present a viewport into the game world.
     */
    CLIENT,
    /**
     * The dedicated server distribution. This is the server only distribution available for
     * download. It simulates the world, and can be communicated with via a network.
     * It contains no visual elements of the game whatsoever.
     */
    DEDICATED_SERVER;

    /**
     * @return If this marks a dedicated server.
     */
    public boolean isDedicatedServer()
    {
        return !isClient();
    }

    /**
     * @return if this marks a client.
     */
    public boolean isClient()
    {
        return this == CLIENT;
    }
}
