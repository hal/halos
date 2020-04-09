package org.wildfly.halos.console;

import javax.inject.Inject;
import javax.inject.Singleton;

import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;
import org.patternfly.components.Navigation;
import org.patternfly.components.NavigationItem;
import org.patternfly.components.Page;
import org.patternfly.resources.Theme;
import org.wildfly.halos.console.resources.Ids;

import static org.patternfly.components.Page.header;
import static org.patternfly.components.Page.main;
import static org.patternfly.components.Page.page;
import static org.patternfly.components.Page.sidebar;

@Singleton
public class Skeleton implements IsElement<HTMLDivElement> {

    private Page page;

    @Inject
    public Skeleton(/*org.treblereel.gwt.crysknife.navigation.client.local.Navigation mainContent*/) {
        Navigation navigation = Navigation.vertical(false)
                .add(new NavigationItem(Ids.SERVER_ITEM, "Server", "#"))
                .add(new NavigationItem(Ids.RESOURCES_ITEM, "Resources", "#"))
                .add(new NavigationItem(Ids.MANAGEMENT_MODEL_ITEM, "Management Model", "#"));

        page = page()
                .add(header("halOS", "#"))
                .add(sidebar(Theme.DARK).add(navigation))
                .add(main(Ids.ROOT_CONTAINER)
                        /*.add(mainContent.getContentPanel())*/);
    }

    @Override
    public HTMLDivElement element() {
        return page.element();
    }
}
