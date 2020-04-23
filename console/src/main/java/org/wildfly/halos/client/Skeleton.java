package org.wildfly.halos.client;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.By;
import org.jboss.elemento.IsElement;
import org.patternfly.components.Navigation;
import org.patternfly.components.NavigationItem;
import org.patternfly.components.Page;
import org.patternfly.resources.Theme;
import org.treblereel.gwt.crysknife.navigation.client.local.api.NavigationSelector;
import org.wildfly.halos.client.resources.Ids;

import static org.patternfly.components.Page.header;
import static org.patternfly.components.Page.main;
import static org.patternfly.components.Page.page;
import static org.patternfly.components.Page.sidebar;
import static org.wildfly.halos.client.resources.Pages.BROWSER;
import static org.wildfly.halos.client.resources.Pages.HOME;
import static org.wildfly.halos.client.resources.Pages.SERVER;
import static org.wildfly.halos.client.resources.Pages.hash;

@Singleton
public class Skeleton implements IsElement<HTMLDivElement> {

    private final Page page;

    @Inject
    public Skeleton(org.treblereel.gwt.crysknife.navigation.client.local.Navigation nav) {
        Navigation navigation = Navigation.vertical(false)
                .add(new NavigationItem(Ids.SERVER_ITEM, "Server", hash(SERVER)))
                .add(new NavigationItem(Ids.MANAGEMENT_MODEL_ITEM, "Management Model", hash(BROWSER)));

        Page.Main main = main(Ids.ROOT_CONTAINER);
        nav.setNavigationContainer(main.element());

        page = page()
                .add(header("halOS", hash(HOME)))
                .add(sidebar(Theme.DARK).add(navigation))
                .add(main);
    }

    @Override
    public HTMLDivElement element() {
        return page.element();
    }

    @Produces
    @NavigationSelector
    public static By navigationSelector() {
        return By.id(Ids.ROOT_CONTAINER);
    }
}
