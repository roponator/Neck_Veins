package de.lessvoid.nifty.renderer.lwjgl.render;

import de.lessvoid.nifty.render.batch.spi.MouseCursorFactory;
import de.lessvoid.nifty.spi.render.MouseCursor;
import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Aaron Mahan &lt;aaron@forerunnergames.com&gt;
 */
public class LwjglMouseCursorFactory implements MouseCursorFactory {

  @Nullable
  @Override
  public MouseCursor create(
          @Nonnull final String filename,
          final int hotspotX,
          final int hotspotY,
          @Nonnull final NiftyResourceLoader resourceLoader) throws IOException {
    return new LwjglMouseCursor(filename, hotspotX, hotspotY, resourceLoader);
  }
}
