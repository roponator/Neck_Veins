package de.lessvoid.nifty.layout.manager;

import de.lessvoid.nifty.layout.Box;
import de.lessvoid.nifty.layout.BoxConstraints;
import de.lessvoid.nifty.layout.LayoutPart;
import de.lessvoid.nifty.layout.align.HorizontalAlign;
import de.lessvoid.nifty.layout.align.VerticalAlign;
import de.lessvoid.nifty.tools.SizeValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * CenterLayout centers all child elements. If there are
 * more than one child elements all elements will be
 * centered (and over layed above each other).
 * <p/>
 * Remember that center probably makes only sense if the
 * centered element has some width and height constraints set.
 *
 * @author void
 */
public class CenterLayout implements LayoutManager {
  /**
   * layoutElements.
   *
   * @param rootElement @see {@link LayoutManager}
   * @param elements    @see {@link LayoutManager}
   */
  @Override
  public void layoutElements(@Nullable final LayoutPart rootElement, @Nullable final List<LayoutPart> elements) {

    // check for useful params
    if (rootElement == null || elements == null || elements.size() == 0) {
      return;
    }

    Box rootBox = rootElement.getBox();
    BoxConstraints rootBoxConstraints = rootElement.getBoxConstraints();

    for (int i = 0; i < elements.size(); i++) {
      layoutElement(elements.get(i), rootBox, rootBoxConstraints);
    }
  }

  private int leftMargin(@Nonnull final BoxConstraints boxConstraints, final int rootBoxWidth) {
    return boxConstraints.getMarginLeft().getValueAsInt(rootBoxWidth);
  }

  private int rightMargin(@Nonnull final BoxConstraints boxConstraints, final int rootBoxWidth) {
    return boxConstraints.getMarginRight().getValueAsInt(rootBoxWidth);
  }

  private int topMargin(@Nonnull final BoxConstraints boxConstraints, final int rootBoxHeight) {
    return boxConstraints.getMarginTop().getValueAsInt(rootBoxHeight);
  }

  private int bottomMargin(@Nonnull final BoxConstraints boxConstraints, final int rootBoxHeight) {
    return boxConstraints.getMarginBottom().getValueAsInt(rootBoxHeight);
  }

  private void layoutElement(
      @Nonnull final LayoutPart element,
      @Nonnull Box rootBox,
      @Nonnull BoxConstraints rootBoxConstraints) {
    Box box = element.getBox();
    BoxConstraints constraint = element.getBoxConstraints();

    if (constraint.getWidth().hasHeightSuffix()) {
      handleVerticalAlignment(rootBox, rootBoxConstraints, box, constraint);
      handleHorizontalAlignment(rootBox, rootBoxConstraints, box, constraint);
    } else if (constraint.getHeight().hasWidthSuffix()) {
      handleHorizontalAlignment(rootBox, rootBoxConstraints, box, constraint);
      handleVerticalAlignment(rootBox, rootBoxConstraints, box, constraint);
    } else {
      handleVerticalAlignment(rootBox, rootBoxConstraints, box, constraint);
      handleHorizontalAlignment(rootBox, rootBoxConstraints, box, constraint);
    }

    box.setX(box.getX() + leftMargin(constraint, rootBox.getWidth()) - rightMargin(constraint, rootBox.getWidth()));
    box.setY(box.getY() + topMargin(constraint, rootBox.getHeight()) - bottomMargin(constraint, rootBox.getHeight()));
  }

  void handleHorizontalAlignment(
      @Nonnull final Box rootBox,
      @Nonnull final BoxConstraints rootBoxConstraints,
      @Nonnull final Box box,
      @Nonnull final BoxConstraints constraint) {
    if (constraint.getWidth().hasValue()) {
      handleWidthConstraint(rootBox, rootBoxConstraints, box, constraint);
    } else {
      box.setX(
          rootBox.getX()
              + rootBoxConstraints.getPaddingLeft().getValueAsInt(rootBox.getWidth()));
      box.setWidth(
          rootBox.getWidth()
              - rootBoxConstraints.getPaddingLeft().getValueAsInt(rootBox.getWidth())
              - rootBoxConstraints.getPaddingRight().getValueAsInt(rootBox.getWidth()));
    }
  }

  void handleVerticalAlignment(
      @Nonnull final Box rootBox,
      @Nonnull final BoxConstraints rootBoxConstraints,
      @Nonnull final Box box,
      @Nonnull final BoxConstraints constraint) {
    if (constraint.getHeight().hasValue()) {
      handleHeightConstraint(rootBox, rootBoxConstraints, box, constraint);
    } else {
      box.setY(
          rootBox.getY()
              + rootBoxConstraints.getPaddingTop().getValueAsInt(rootBox.getHeight()));
      box.setHeight(
          rootBox.getHeight()
              - rootBoxConstraints.getPaddingTop().getValueAsInt(rootBox.getHeight())
              - rootBoxConstraints.getPaddingBottom().getValueAsInt(rootBox.getHeight()));
    }
  }

  private void handleWidthConstraint(
      @Nonnull final Box rootBox,
      @Nonnull final BoxConstraints rootBoxConstraints,
      @Nonnull final Box box,
      @Nonnull final BoxConstraints constraint) {
    int rootBoxX = rootBox.getX() + rootBoxConstraints.getPaddingLeft().getValueAsInt(rootBox.getWidth());
    int rootBoxWidth = rootBox.getWidth() - rootBoxConstraints.getPaddingLeft().getValueAsInt(rootBox.getWidth()) -
        rootBoxConstraints.getPaddingRight().getValueAsInt(rootBox.getWidth());

    int boxWidth = constraint.getWidth().getValueAsInt(rootBoxWidth);
    if (constraint.getWidth().hasHeightSuffix()) {
      boxWidth = constraint.getWidth().getValueAsInt(box.getHeight());
    }
    box.setWidth(boxWidth);

    if (constraint.getHorizontalAlign() == HorizontalAlign.left) {
      box.setX(rootBoxX);
    } else if (constraint.getHorizontalAlign() == HorizontalAlign.right) {
      box.setX(rootBoxX + rootBox.getWidth() - rootBoxConstraints.getPaddingRight().getValueAsInt(rootBox.getWidth())
          - boxWidth);
    } else {
      // default and center is the same in here
      box.setX(rootBoxX + (rootBoxWidth - boxWidth) / 2);
    }
  }

  private void handleHeightConstraint(
      @Nonnull final Box rootBox,
      @Nonnull final BoxConstraints rootBoxConstraints,
      @Nonnull final Box box,
      @Nonnull final BoxConstraints constraint) {
    int rootBoxY = rootBox.getY() + rootBoxConstraints.getPaddingTop().getValueAsInt(rootBox.getHeight());
    int rootBoxHeight = rootBox.getHeight() - rootBoxConstraints.getPaddingTop().getValueAsInt(rootBox.getHeight()) -
        rootBoxConstraints.getPaddingBottom().getValueAsInt(rootBox.getHeight());

    int boxHeight = constraint.getHeight().getValueAsInt(rootBoxHeight);
    if (constraint.getHeight().hasWidthSuffix()) {
      boxHeight = constraint.getHeight().getValueAsInt(box.getWidth());
    }
    box.setHeight(boxHeight);

    if (constraint.getVerticalAlign() == VerticalAlign.top) {
      box.setY(rootBoxY);
    } else if (constraint.getVerticalAlign() == VerticalAlign.bottom) {
      box.setY(rootBoxY + rootBox.getHeight() - rootBoxConstraints.getPaddingBottom().getValueAsInt(rootBox.getHeight
          ()) - boxHeight);
    } else {
      // center is default in here
      box.setY(rootBoxY + (rootBoxHeight - boxHeight) / 2);
    }
  }

  /**
   * @param children children elements of the root element
   * @return new calculated SizeValue
   */
  @Nonnull
  @Override
  public SizeValue calculateConstraintWidth(@Nonnull final LayoutPart root, @Nonnull final List<LayoutPart> children) {
    return root.getMaxWidth(children);
  }

  /**
   * @param children children elements of the root element
   * @return new calculated SizeValue
   */
  @Nonnull
  @Override
  public SizeValue calculateConstraintHeight(@Nonnull final LayoutPart root, @Nonnull final List<LayoutPart> children) {
    return root.getMaxHeight(children);
  }
}
