package org.wildfly.halos.client.browser;

import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

public class ModelBrowser implements IsElement<HTMLElement> {

    private final HTMLElement root;

    public ModelBrowser() {
        root = div().element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
