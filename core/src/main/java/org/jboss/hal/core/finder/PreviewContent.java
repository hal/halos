/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.core.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ExternalTextResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.builder.ElementsBuilder;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.PatternFly;
import org.wildfly.halos.resources.CSS;
import org.jboss.hal.spi.Callback;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;

/** Wrapper for the preview content which consists of a header (mandatory) and one or more optional elements. */
public class PreviewContent<T> implements Iterable<HTMLElement>, Attachable {

    /** Common building block for a refresh link */
    protected static HTMLElement refreshLink(Callback callback) {
        return a().css(clickable, pullRight, smallLink, marginTop5).on(click, event -> callback.execute())
                .add(span().css(fontAwesome("refresh"), marginRight5))
                .add(span().textContent(CONSTANTS.refresh()))
                .get();

    }


    private static final int MAX_HEADER_LENGTH = 30;
    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final List<Attachable> attachables;
    private final ElementsBuilder builder;
    private HTMLElement headerContainer;
    private HTMLElement lead;


    // ------------------------------------------------------ construction

    /**
     * Empty preview w/o content
     */
    public PreviewContent(String header) {
        this(header, (String) null);
    }

    public PreviewContent(String header, String lead) {
        attachables = new ArrayList<>();
        builder = collect().add(header(header));
        if (lead != null) {
            builder.add(lead(lead));
        }
    }

    public PreviewContent(String header, SafeHtml html) {
        this(header, null, html);
    }

    public PreviewContent(String header, String lead, SafeHtml html) {
        attachables = new ArrayList<>();
        builder = collect().add(header(header));
        if (lead != null) {
            builder.add(lead(lead));
        }
        builder.add(section().innerHtml(html));
    }

    public PreviewContent(String header, HTMLElement first, HTMLElement... rest) {
        this(header, null, first, rest);
    }

    public PreviewContent(String header, String lead, HTMLElement first, HTMLElement... rest) {
        attachables = new ArrayList<>();
        builder = collect().add(header(header));
        if (lead != null) {
            builder.add(lead(lead));
        }

        HtmlContentBuilder<HTMLElement> section;
        builder.add(section = section().add(first));
        if (rest != null) {
            for (HTMLElement element : rest) {
                section.add(element);
            }
        }
    }

    public PreviewContent(String header, Iterable<HTMLElement> elements) {
        this(header, null, elements);
    }

    public PreviewContent(String header, String lead, Iterable<HTMLElement> elements) {
        attachables = new ArrayList<>();
        builder = collect().add(header(header));
        if (lead != null) {
            builder.add(lead(lead));
        }

        builder.add(section().addAll(elements));
    }

    public PreviewContent(String header, ExternalTextResource resource) {
        this(header, null, resource);
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    public PreviewContent(String header, String lead, ExternalTextResource resource) {
        attachables = new ArrayList<>();
        builder = collect().add(header(header));
        if (lead != null) {
            builder.add(lead(lead));
        }

        HTMLElement section;
        builder.add(section = section().get());
        Previews.innerHtml(section, resource);
    }


    // ------------------------------------------------------ header & lead

    protected HTMLElement getHeaderContainer() {
        return headerContainer;
    }

    protected HTMLElement getLeadElement() {
        return lead;
    }

    private HTMLElement header(String header) {
        String readableHeader = shorten(header);
        HtmlContentBuilder<HTMLElement> builder = span();
        if (!readableHeader.equals(header)) {
            builder.textContent(readableHeader);
            builder.title(header);
        } else {
            builder.textContent(header);
        }
        return this.headerContainer = h(1).add(builder.get()).get();
    }

    private String shorten(String header) {
        return header.length() > MAX_HEADER_LENGTH
                ? Strings.abbreviateMiddle(header, MAX_HEADER_LENGTH)
                : header;
    }

    private HTMLElement lead(String lead) {
        return this.lead = p().css(CSS.lead).textContent(lead).get();
    }


    // ------------------------------------------------------ other methods

    protected ElementsBuilder previewBuilder() {
        return builder;
    }

    protected void registerAttachable(Attachable first, Attachable... rest) {
        attachables.add(first);
        if (rest != null) {
            Collections.addAll(attachables, rest);
        }
    }

    @Override
    public void attach() {
        PatternFly.initComponents("." + finderPreview);
        attachables.forEach(Attachable::attach);
    }

    @Override
    public void detach() {
        attachables.forEach(Attachable::detach);
    }

    @Override
    public Iterator<HTMLElement> iterator() {
        return builder.get().iterator();
    }

    @SuppressWarnings("UnusedParameters")
    public void update(T item) {
    }
}
