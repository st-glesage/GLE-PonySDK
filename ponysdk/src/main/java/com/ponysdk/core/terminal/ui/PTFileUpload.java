/*
 * Copyright (c) 2011 PonySDK
 *  Owners:
 *  Luciano Broussal  <luciano.broussal AT gmail.com>
 *  Mathieu Barbier   <mathieu.barbier AT gmail.com>
 *  Nicolas Ciaravola <nicolas.ciaravola.pro AT gmail.com>
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

package com.ponysdk.core.terminal.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.ponysdk.core.model.ClientToServerModel;
import com.ponysdk.core.model.HandlerModel;
import com.ponysdk.core.model.ServerToClientModel;
import com.ponysdk.core.terminal.PonySDK;
import com.ponysdk.core.terminal.instruction.PTInstruction;
import com.ponysdk.core.terminal.model.BinaryModel;
import com.ponysdk.core.terminal.model.ReaderBuffer;

public class PTFileUpload extends PTWidget<FormPanel> {

    private FileUpload fileUpload;

    @Override
    protected FormPanel createUIObject() {
        fileUpload = new FileUpload();

        final FormPanel formPanel = new FormPanel();
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        formPanel.setMethod(FormPanel.METHOD_POST);

        final VerticalPanel panel = new VerticalPanel();
        panel.setSize("100%", "100%");
        panel.add(fileUpload);
        formPanel.setWidget(panel);

        formPanel.addSubmitCompleteHandler(event -> {
            final PTInstruction instruction = new PTInstruction(objectID);
            instruction.put(ClientToServerModel.HANDLER_SUBMIT_COMPLETE);
            uiBuilder.sendDataToServer(uiObject, instruction);
        });

        return formPanel;
    }

    @Override
    public boolean update(final ReaderBuffer buffer, final BinaryModel binaryModel) {
        final int modelOrdinal = binaryModel.getModel().ordinal();
        if (ServerToClientModel.NAME.ordinal() == modelOrdinal) {
            fileUpload.setName(binaryModel.getStringValue());
            return true;
        } else if (ServerToClientModel.ENABLED.ordinal() == modelOrdinal) {
            fileUpload.setEnabled(binaryModel.getBooleanValue());
            return true;
        } else {
            return super.update(buffer, binaryModel);
        }
    }

    @Override
    public void addHandler(final ReaderBuffer buffer, final HandlerModel handlerModel) {
        if (HandlerModel.HANDLER_CHANGE.equals(handlerModel)) {
            fileUpload.addChangeHandler(event -> {
                final PTInstruction eventInstruction = new PTInstruction(getObjectID());
                eventInstruction.put(ClientToServerModel.HANDLER_CHANGE, fileUpload.getFilename());
                uiBuilder.sendDataToServer(fileUpload, eventInstruction);
            });
        } else if (HandlerModel.HANDLER_EMBEDED_STREAM_REQUEST.equals(handlerModel)) {
            // ServerToClientModel.STREAM_REQUEST_ID
            final int streamRequestId = buffer.readBinaryModel().getIntValue();

            final String action = GWT.getHostPageBaseURL() + "stream?" + ClientToServerModel.UI_CONTEXT_ID.toStringValue() + "="
                    + PonySDK.get().getContextId() + "&" + ClientToServerModel.STREAM_REQUEST_ID.toStringValue() + "="
                    + streamRequestId;
            uiObject.setAction(action);
            uiObject.submit();
        } else {
            super.addHandler(buffer, handlerModel);
        }
    }

    @Override
    public void removeHandler(final ReaderBuffer buffer, final HandlerModel handlerModel) {
        if (HandlerModel.HANDLER_CHANGE.equals(handlerModel)) {
            // TODO Remove HANDLER_CHANGE
        } else {
            super.removeHandler(buffer, handlerModel);
        }
    }

}
