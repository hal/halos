package org.wildfly.halos.client.server;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLPreElement;
import org.jboss.elemento.HtmlContentBuilder;
import org.jboss.elemento.IsElement;
import org.treblereel.gwt.crysknife.navigation.client.local.Page;
import org.wildfly.halos.client.dmr.Dispatcher;
import org.wildfly.halos.client.dmr.Operation;
import org.wildfly.halos.client.dmr.ResourceAddress;
import org.wildfly.halos.client.resources.Pages;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.pre;
import static org.patternfly.components.Content.content;
import static org.patternfly.components.Page.section;
import static org.patternfly.resources.CSS.modifier;
import static org.patternfly.resources.Constants.light;
import static org.wildfly.halos.client.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.wildfly.halos.client.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.wildfly.halos.client.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

@Singleton
@Page(path = Pages.SERVER)
public class ServerPage implements IsElement<HTMLElement> {

    private final HTMLElement root;
    private final HtmlContentBuilder<HTMLPreElement> pre;
    @Inject Dispatcher dispatcher;

    public ServerPage() {
        root = section().css(modifier(light))
                .add(content()
                        .add(h(1, "Server"))
                        .add(p().textContent("Pending...."))
                        .add(pre = pre()))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @PostConstruct
    void init() {
        Operation operation = new Operation.Builder(ResourceAddress.root(), READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(ATTRIBUTES_ONLY, true)
                .build();
        dispatcher.execute(operation).then(result -> {
            pre.textContent(result.toJSONString());
            return null;
        });
    }
}
