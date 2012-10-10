
package com.ponysdk.ui.server.list2.selector;

import com.ponysdk.core.tools.ListenerCollection;
import com.ponysdk.ui.server.basic.PAnchor;
import com.ponysdk.ui.server.basic.PHorizontalPanel;
import com.ponysdk.ui.server.basic.PLabel;
import com.ponysdk.ui.server.basic.PWidget;
import com.ponysdk.ui.server.basic.event.PClickEvent;
import com.ponysdk.ui.server.basic.event.PClickHandler;
import com.ponysdk.ui.server.list.SelectionMode;
import com.ponysdk.ui.terminal.basic.PHorizontalAlignment;

public class DefaultInfoSelectorView extends PHorizontalPanel implements SelectorView {

    private final ListenerCollection<SelectorViewListener> selectorViewListeners = new ListenerCollection<SelectorViewListener>();

    final PLabel numberOfSelectedMessage = new PLabel();
    final PAnchor selectAllAnchor = new PAnchor();
    final PAnchor selectNoneAnchor = new PAnchor();

    public DefaultInfoSelectorView() {
        setHorizontalAlignment(PHorizontalAlignment.ALIGN_CENTER);
        setStyleName("pony-ComplexList-OptionSelectionPanel");
        setVisible(false);
        selectAllAnchor.addClickHandler(new PClickHandler() {

            @Override
            public void onClick(final PClickEvent event) {
                for (final SelectorViewListener listener : selectorViewListeners) {
                    listener.onSelectionChange(SelectionMode.FULL);
                }
            }
        });
        selectNoneAnchor.addClickHandler(new PClickHandler() {

            @Override
            public void onClick(final PClickEvent event) {
                for (final SelectorViewListener listener : selectorViewListeners) {
                    listener.onSelectionChange(SelectionMode.NONE);
                }
            }
        });
    }

    @Override
    public PWidget asWidget() {
        return this;
    }

    @Override
    public void addSelectorViewListener(final SelectorViewListener selectorViewListener) {
        selectorViewListeners.register(selectorViewListener);
    }

    @Override
    public void update(final SelectionMode selectionMode, final int numberOfSelectedItems, final int fullSize, final int pageSize) {
        switch (selectionMode) {
            case FULL:
                setVisible(true);
                clear();
                add(numberOfSelectedMessage);

                numberOfSelectedMessage.setText("All " + numberOfSelectedItems + " items are selected.");
                if (numberOfSelectedItems > pageSize) {
                    selectNoneAnchor.setText("Clear Selection");
                    add(selectNoneAnchor);
                }
                break;
            case NONE:
                if (isVisible()) setVisible(false);
                break;
            case PAGE:
                setVisible(true);
                clear();
                add(numberOfSelectedMessage);
                numberOfSelectedMessage.setText("All " + numberOfSelectedItems + " items on this page are selected.");

                final int itemsLeft = fullSize - numberOfSelectedItems;
                selectAllAnchor.setText("Select all " + itemsLeft + " final items");
                add(selectAllAnchor);
                break;
            case PARTIAL:
                if (isVisible()) setVisible(false);
                break;
        }
    }

}
