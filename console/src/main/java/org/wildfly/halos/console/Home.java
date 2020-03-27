package org.wildfly.halos.console;

import javax.inject.Singleton;

import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;
import org.treblereel.gwt.crysknife.navigation.client.local.DefaultPage;
import org.treblereel.gwt.crysknife.navigation.client.local.Page;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.patternfly.components.Content.content;
import static org.patternfly.components.Page.section;
import static org.patternfly.resources.CSS.modifier;
import static org.patternfly.resources.Constants.light;

@Singleton
@Page(path = "home", role = DefaultPage.class)
public class Home implements IsElement<HTMLElement> {

    private HTMLElement root;

    public Home() {
        root = section().css(modifier(light))
                .add(content()
                        .add(h(1, "halOS"))
                        .add(p().textContent("WildFly management console for OpenShift.")))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
