package de.lessvoid.nifty.html;

import de.lessvoid.nifty.builder.EffectBuilder;
import de.lessvoid.nifty.builder.ElementBuilder.Align;
import de.lessvoid.nifty.builder.ImageBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.TextBuilder;


import java.util.logging.Logger;

public class NiftyBuilderFactory {
  private final Logger log = Logger.getLogger(NiftyBuilderFactory.class.getName());

  public PanelBuilder createBodyPanelBuilder() {
    PanelBuilder builder = createPanelBuilder();
    builder.width("100%");
    builder.height("100%");
    builder.childLayoutVertical();
    return builder;
  }

  public PanelBuilder createParagraphPanelBuilder() {
    PanelBuilder builder = createPanelBuilder();
    builder.width("100%");
    builder.childLayoutVertical();
    return builder;
  }

  public TextBuilder createTextBuilder(final String text,  final String defaultFontName,  final String color) {
    TextBuilder textBuilder = createTextBuilder();
    textBuilder.text(text);
    textBuilder.wrap(true);
    textBuilder.alignLeft();
    textBuilder.valignTop();
    textBuilder.textHAlignLeft();
    textBuilder.textVAlignTop();
    textBuilder.font(defaultFontName);
    textBuilder.width("100%");
    if (color != null) {
      textBuilder.color(color);
    }
    return textBuilder;
  }

  public ImageBuilder createImageBuilder( final String src,  final String align,  final String width,  final String height,  final String bgcolor,  final String vspace) {
    ImageBuilder imageBuilder = createImageBuilder();
    imageBuilder.filename(src);
    if (align != null) {
      imageBuilder.align(translateAlign(align));
    }
    if (width != null) {
      imageBuilder.width(width);
    }
    if (height != null) {
      imageBuilder.height(height);
    }
    if (bgcolor != null) {
      imageBuilder.backgroundColor(bgcolor);
    }
    if (vspace != null) {
      imageBuilder.padding(vspace);
    }
    return imageBuilder;
  }

  public PanelBuilder createBreakPanelBuilder( final String height) {
    PanelBuilder result = createPanelBuilder();
    result.height(height);
    return result;
  }

  public PanelBuilder createTableTagPanelBuilder(final String width, final String bgcolor, final String border, final String bordercolor) {
    PanelBuilder result = createPanelBuilder();
    result.childLayoutVertical();
    addTableGeneralAttributes(width, bgcolor, border, bordercolor, result);
    return result;
  }

  public PanelBuilder createTableRowPanelBuilder(final String width, final String bgcolor, final String border, final String bordercolor) {
    PanelBuilder result = createPanelBuilder();
    result.childLayoutHorizontal();
    addTableGeneralAttributes(width, bgcolor, border, bordercolor, result);
    return result;
  }

  public PanelBuilder createTableDataPanelBuilder(final String width, final String bgcolor, final String border, final String bordercolor) {
    PanelBuilder result = createPanelBuilder();
    result.childLayoutVertical();
    addTableGeneralAttributes(width, bgcolor, border, bordercolor, result);
    return result;
  }

  PanelBuilder createPanelBuilder() {
    return new PanelBuilder();
  }

  TextBuilder createTextBuilder() {
    return new TextBuilder();
  }

  ImageBuilder createImageBuilder() {
    return new ImageBuilder();
  }

  
  private Align translateAlign(final String align) {
    if ("left".equalsIgnoreCase(align)) {
      return Align.Left;
    } else if ("right".equalsIgnoreCase(align)) {
        return Align.Right;
    } else if ("middle".equalsIgnoreCase(align)) {
      return Align.Center;
    } else {
      // default to left
      log.warning("Unknown align type [" + align + "] detected. Will default to Align.LEFT");
      return Align.Left;
    }
  }

  private void addTableGeneralAttributes(
       final String width,
       final String bgcolor,
       final String border,
       final String bordercolor,
       PanelBuilder result) {
    if (width != null) {
      result.width(width);
    }
    if (bgcolor != null) {
      result.backgroundColor(bgcolor);
    }
    if (border != null) {
      result.onActiveEffect(new EffectBuilder("border") {{
        effectParameter("border", border);
        if (bordercolor != null) {
          effectParameter("color", bordercolor);
        }
      }});
    }
  }
}
