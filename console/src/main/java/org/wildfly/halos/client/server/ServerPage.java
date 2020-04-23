package org.wildfly.halos.client.server;

import javax.inject.Singleton;

import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;
import org.treblereel.gwt.crysknife.navigation.client.local.Page;
import org.wildfly.halos.client.resources.Pages;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.patternfly.components.Content.content;
import static org.patternfly.components.Page.section;
import static org.patternfly.resources.CSS.modifier;
import static org.patternfly.resources.Constants.light;

@Singleton
@Page(path = Pages.SERVER)
public class ServerPage implements IsElement<HTMLElement> {

    private final HTMLElement root;

    public ServerPage() {
        root = section().css(modifier(light))
                .add(content()
                        .add(h(1, "Server"))
                        .add(p().textContent("Pending....")))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
