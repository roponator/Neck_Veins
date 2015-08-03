package de.lessvoid.nifty;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This generator is used to create the IDs for Nifty-GUI elements that did not get a specific ID. It simply creates
 * new numbered IDs.
 *
 * @author void
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class NiftyIdCreator {
  /**
   * The provider for the IDs.
   */
  private static final AtomicLong nextId = new AtomicLong(1);

  /**
   * Generate a new ID.
   *
   * @return the string containing the new id.
   */
  @Nonnull
  public static String generate() {
    final long id = nextId.getAndIncrement();
    return Long.toString(id);
  }

  /**
   * Check if the ID supplied as string is a ID that is likely to be generated by this generator.
   *
   * @param stringId the string representation of the ID
   * @return {@code true} if the ID is likely a generated one, note that false positives are possible
   */
  public static boolean isGeneratedId(@Nonnull final String stringId) {
    try {
      final long id = Long.parseLong(stringId);
      final long currentId = nextId.get();
      return id < currentId;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
