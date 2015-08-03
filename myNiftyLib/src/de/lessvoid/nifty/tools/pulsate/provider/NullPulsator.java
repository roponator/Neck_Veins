package de.lessvoid.nifty.tools.pulsate.provider;

import de.lessvoid.nifty.tools.pulsate.PulsatorProvider;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * The NullPulsator does not really pulsate =).
 *
 * @author void
 */
public class NullPulsator implements PulsatorProvider {

  /**
   * Actually does nothing.
   *
   * @param parameter the parameters
   */
  @Override
  public void initialize(@Nonnull final Properties parameter) {
  }

  /**
   * Always returns 0.
   *
   * @param msTime the time
   * @return always returns 0
   */
  @Override
  public float getValue(final long msTime) {
    return 0;
  }

  /**
   * Reset.
   *
   * @param msTime the time
   */
  @Override
  public void reset(final long msTime) {
  }
}
