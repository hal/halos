package org.wildfly.halos.console;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.gwtproject.core.client.EntryPoint;
import org.treblereel.gwt.crysknife.client.Application;
import org.treblereel.gwt.crysknife.client.ComponentScan;
import org.wildfly.halos.console.config.Instances;

import static org.jboss.elemento.Elements.body;
import static org.patternfly.components.AlertGroup.toast;

@Application
@ComponentScan("org.wildfly.halos.console")
public class Console implements EntryPoint {

    @Inject Instances instances;
    @Inject Skeleton skeleton;

    @Override
    public void onModuleLoad() {
        new ConsoleBootstrap(this).initialize();
    }

    @PostConstruct
    void init() {
        instances.init();
        instances.subscribe();
        body().addAll(skeleton, toast());
    }
}
