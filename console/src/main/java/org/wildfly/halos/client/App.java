package org.wildfly.halos.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.EntryPoint;
import org.treblereel.gwt.crysknife.client.Application;
import org.treblereel.gwt.crysknife.client.ComponentScan;
import org.treblereel.gwt.crysknife.navigation.client.local.DefaultPage;
import org.treblereel.gwt.crysknife.navigation.client.local.Navigation;
import org.wildfly.halos.client.config.Instances;

import static org.jboss.elemento.Elements.body;
import static org.patternfly.components.AlertGroup.toast;

@Application
@ComponentScan("org.wildfly.halos.console")
public class App implements EntryPoint {

    @Inject Instances instances;
    @Inject Skeleton skeleton;
    @Inject Navigation navigation;

    @Override
    public void onModuleLoad() {
        new AppBootstrap(this).initialize();
    }

    @PostConstruct
    void init() {
        instances.init();
        instances.subscribe();
        body().addAll(skeleton, toast());
        navigation.goToWithRole(DefaultPage.class);
    }
}
