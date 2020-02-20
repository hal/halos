package org.wildfly.halos.console;

import org.gwtproject.core.client.EntryPoint;
import org.patternfly.components.Navigation;
import org.patternfly.components.NavigationItem;
import org.patternfly.components.Page;
import org.wildfly.halos.resources.Ids;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.nav;
import static org.jboss.elemento.Elements.p;
import static org.patternfly.components.AlertGroup.toast;
import static org.patternfly.components.Content.content;
import static org.patternfly.components.Page.*;
import static org.patternfly.resources.CSS.modifier;
import static org.patternfly.resources.Constants.light;

public class Console implements EntryPoint {

    @Override
    public void onModuleLoad() {
        Navigation navigation = Navigation.vertical(false)
                .add(new NavigationItem(Ids.SERVER_ITEM, "Server", "#"))
                .add(new NavigationItem(Ids.RESOURCES_ITEM, "Resources", "#"))
                .add(new NavigationItem(Ids.MANAGEMENT_MODEL_ITEM, "Management Model", "#"));

        Page page = page()
                .add(header("halOS", "#"))
                .add(sidebar().add(navigation))
                .add(main(Ids.ROOT_CONTAINER)
                        .add(section().css(modifier(light))
                                .add(content()
                                        .add(h(1, "halOS"))
                                        .add(p().textContent("WildFly management console for OpenShift.")))));

        body().addAll(page, toast());
    }
}
