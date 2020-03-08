package org.wildfly.halos.console;

import elemental2.dom.HTMLPreElement;
import org.gwtproject.core.client.EntryPoint;
import org.patternfly.components.Navigation;
import org.patternfly.components.NavigationItem;
import org.patternfly.components.Page;
import org.treblereel.gwt.crysknife.client.Application;
import org.wildfly.halos.console.dispatch.Dispatcher;
import org.wildfly.halos.console.dmr.Operation;
import org.wildfly.halos.console.dmr.ResourceAddress;
import org.wildfly.halos.console.resources.Ids;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.pre;
import static org.patternfly.components.AlertGroup.toast;
import static org.patternfly.components.Content.content;
import static org.patternfly.components.Page.*;
import static org.patternfly.resources.CSS.modifier;
import static org.patternfly.resources.Constants.light;
import static org.wildfly.halos.console.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

@Application
public class Console implements EntryPoint {

    @Override
    public void onModuleLoad() {
        Navigation navigation = Navigation.vertical(false)
                .add(new NavigationItem(Ids.SERVER_ITEM, "Server", "#"))
                .add(new NavigationItem(Ids.RESOURCES_ITEM, "Resources", "#"))
                .add(new NavigationItem(Ids.MANAGEMENT_MODEL_ITEM, "Management Model", "#"));

        HTMLPreElement dmr;
        String property = System.getProperty("halos.endpoints.management");
        Page page = page()
                .add(header("halOS", "#"))
                .add(sidebar().add(navigation))
                .add(main(Ids.ROOT_CONTAINER)
                        .add(section().css(modifier(light))
                                .add(content()
                                        .add(h(1, "halOS"))
                                        .add(p().textContent("WildFly management console for OpenShift."))
                                        .add(p().textContent("property: '" + property + "'."))
                                        .add(dmr = pre().element()))));

        body().addAll(page, toast());

        Dispatcher dispatcher = new Dispatcher();
        Operation operation = new Operation.Builder(ResourceAddress.root(), READ_RESOURCE_OPERATION).build();
        dispatcher.execute(operation)
                .then(result -> {
                    dmr.textContent = result.toString();
                    return null;
                })
                .catch_(error -> {
                    dmr.textContent = String.valueOf(error);
                    return null;
                });
    }
}
