/*
 * Copyright (c) 2021 PonySDK
 *  Owners:
 *  Luciano Broussal  <luciano.broussal AT gmail.com>
 *	Mathieu Barbier   <mathieu.barbier AT gmail.com>
 *	Nicolas Ciaravola <nicolas.ciaravola.pro AT gmail.com>
 *
 *  WebSite:
 *  http://code.google.com/p/pony-sdk/
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ponysdk.core.ui.infinitescroll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.JsonObject;

import com.ponysdk.core.ui.basic.Element;
import com.ponysdk.core.ui.basic.IsPWidget;
import com.ponysdk.core.ui.basic.PAddOnComposite;
import com.ponysdk.core.ui.basic.PFlowPanel;
import com.ponysdk.core.ui.basic.PPanel;

/**
 * @author mzoughagh
 * @param <D> Data
 * @param <W> Widget
 */
public class InfiniteScrollAddon<D, W extends IsPWidget> extends PAddOnComposite<PPanel> {

    private static final String FUNCTION_ON_DRAW = "onDraw";
    private static final String FUNCTION_SET_SIZE = "setSize";
    private static final String FUNCTION_SET_SCROLL_TOP = "setScrollTop";

    private static final String KEY_MAX_VISIBLE_ITEM = "maxVisibleItem";
    private static final String KEY_BEGIN_INDEX = "beginIndex";

    private static final String STYLE_IS_ITEM = "is-item";
    private static final String STYLE_IS_LOADING = "is-loading";
    private static final String STYLE_IS_CONTAINER = "is-container";
    private static final String STYLE_IS_VIEWPORT = "is-viewport";

    //UI
    private final PFlowPanel container;

    // widgets
    private final List<W> rows = new ArrayList<>();

    // used for drawing
    private int beginIndex = 0;
    private int maxVisibleItems = 10;

    private final InfiniteScrollProvider<D, W> dataProvider;
    private int fullSize;

    /**
     * Creates an infiniteScrollAddon with the specified dataProvier
     *
     * @param dataProvider
     *            infiniteScrollAddon's dataProvier
     */

    public InfiniteScrollAddon(final InfiniteScrollProvider<D, W> dataProvider) {
        super(Element.newPFlowPanel());
        this.dataProvider = dataProvider;
        dataProvider.addHandler(e -> {
            this.maxVisibleItems = this.maxVisibleItems - 1;
            this.refresh();
        });

        widget.addStyleName(STYLE_IS_VIEWPORT);

        container = Element.newPFlowPanel();
        container.addStyleName(STYLE_IS_CONTAINER);
        widget.add(container);

        // HANDLER
        this.setTerminalHandler((event) -> {
            final JsonObject jsonObj = event.getData();
            beginIndex = jsonObj.getInt(KEY_BEGIN_INDEX);
            maxVisibleItems = jsonObj.getInt(KEY_MAX_VISIBLE_ITEM);
            draw();
        });
    }

    /**
     * Redrawing the items after modifying parameters
     * like beginIndex and maxvisibleItem or length dataProvider's list
     */
    public void refresh() {
        dataProvider.getFullSize(this::setFullSize);
    }

    public void setScrollTop() {
        callTerminalMethod(FUNCTION_SET_SCROLL_TOP);
    }

    public void setMaxItemVisible(final int maxVisibleItems) {
        this.maxVisibleItems = maxVisibleItems;
    }

    /**
     * Adding widgets to our DOM by taking into consideration
     * the number of widget (maxVisibleItem) and the beginning
     * index (beginIndex).
     * After adding, it updates existing widgets and removes
     * unused widgets.
     */
    public void draw() {
        final int size = Math.min(maxVisibleItems, fullSize - beginIndex);
        if (size > 0) {
            widget.addStyleName(STYLE_IS_LOADING);
            dataProvider.getData(beginIndex, size, this::draw);
        } else {
            this.draw(Collections.emptyList());
        }
    }

    /**
     * Setting Infinite Scroll visibility
     *
     * @param visible boolean
     */
    public void setVisible(final boolean visible) {
        if (visible) {
            widget.setVisible(true);
        } else {
            widget.setVisible(false);
        }
    }

    /**
     * Get Infinite Scroll visibility
     *
     * @return boolean
     */
    public boolean isVisible() {
        return widget.isVisible();
    }

    public void setStyleProperty(final String name, final String value) {
        widget.setStyleProperty(name, value);
    }

    private void setFullSize(final int fullSize) {
        if (this.fullSize != fullSize) {
            final boolean forceDraw = this.fullSize == 0 || this.fullSize < maxVisibleItems;
            this.fullSize = fullSize;
            if (forceDraw) {
                this.beginIndex = 0;
                draw();
            }
            callTerminalMethod(FUNCTION_SET_SIZE, fullSize);
        } else {
            draw();
        }
    }

    private void addWidgetToContainer(final int index, final W widget) {
        container.insert(widget.asWidget(), index);
        widget.asWidget().addStyleName(STYLE_IS_ITEM);
    }

    private void removeWidgetFromContainer(final W widget) {
        container.remove(widget.asWidget());
        widget.asWidget().onDestroy();
    }

    private void draw(final List<D> data) {
        try {
            final int size = Math.min(maxVisibleItems, fullSize - beginIndex);

            if (data.size() != size) {
                throw new IllegalStateException("Data size doesn't match expected :" + size + ". Actual : " + data.size());
            }

            int fullDataIndex = beginIndex;
            int index = 0;
            //update existing widget
            while (index < Math.min(data.size(), rows.size())) {
                final W currentWidget = rows.get(index);
                final W newWidget = dataProvider.handleUI(fullDataIndex, data.get(index), currentWidget);
                if (currentWidget != newWidget) {
                    removeWidgetFromContainer(currentWidget);
                    rows.set(index, newWidget);
                    addWidgetToContainer(index, newWidget);
                }
                index++;
                fullDataIndex++;
            }

            // create missing widget
            while (rows.size() < data.size()) {
                final W newWidget = dataProvider.handleUI(fullDataIndex, data.get(index), null);
                rows.add(newWidget);
                addWidgetToContainer(index, newWidget);
                index++;
                fullDataIndex++;
            }

            // delete unused widget
            while (data.size() < rows.size()) {
                removeWidgetFromContainer(rows.remove(index));
            }
            callTerminalMethod(FUNCTION_ON_DRAW);
        } finally {
            widget.removeStyleName(STYLE_IS_LOADING);
        }
    }
}