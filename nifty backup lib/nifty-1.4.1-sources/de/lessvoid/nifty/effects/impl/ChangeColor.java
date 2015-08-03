package de.lessvoid.nifty.effects.impl;


import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.effects.EffectImpl;
import de.lessvoid.nifty.effects.EffectProperties;
import de.lessvoid.nifty.effects.Falloff;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.render.NiftyRenderEngine;
import de.lessvoid.nifty.tools.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * ChangeColor Effect - changes the current color including alpha.
 *
 * @author void
 */
public class ChangeColor implements EffectImpl {
  private Color color;

  @Override
  public void activate(
      @Nonnull final Nifty nifty,
      @Nonnull final Element element,
      @Nonnull final EffectProperties parameter) {
    color = new de.lessvoid.nifty.tools.Color(parameter.getProperty("color", "#ffff"));
  }

  @Override
  public void execute(
      @Nonnull final Element element,
      final float normalizedTime,
      @Nullable final Falloff falloff,
      @Nonnull final NiftyRenderEngine r) {
    r.setColor(color);
  }

  @Override
  public void deactivate() {
  }
}
