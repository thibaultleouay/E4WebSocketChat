/*******************************************************************************
 * Copyright (c) 2016 Thibault Le Ouay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - initial API and implementation
 *******************************************************************************/
package fr.thibaultleouay.chat.client.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;

public class SamplePart {

	private Text textInput;
	private StyledText textViewer;
	private Button buttonSend;
	private WebSocket websocket;

	@Inject
	private MDirtyable dirty;

	@PostConstruct
	public void createComposite(Composite parent) {

		GridLayoutFactory.fillDefaults().applyTo(parent);

		textViewer = new StyledText(parent, SWT.V_SCROLL | SWT.WRAP);
		textViewer.setText("Start Chatting :");

		textViewer.setEditable(false);
		textViewer.setEnabled(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(textViewer);

		textInput = new Text(parent, SWT.BORDER);
		textInput.setMessage("Start entering text");
		textInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		buttonSend = new Button(parent, SWT.BORDER);
		buttonSend.setText("Send");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonSend);
		buttonSend.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// When we click our button we send our text input to the websocket
				websocket.writeFinalTextFrame(textInput.getText());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		final Display display = Display.getCurrent();
		// We create our http client 
		HttpClient client = Vertx.vertx().createHttpClient();

		// We create our websocket on ws://localhost:8080 
		client.websocket(8080, "localhost", "", websocket -> {
			websocket.handler(data -> {
				//When we receive data from our websocket we update our widget though we should do it on the SWT thread 
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						textViewer.setText(textViewer.getText()+ "\n"+data.toString("UTF-8"));
					}
				});

			});

			this.websocket = websocket;
		});
	}
	@Focus
	public void setFocus() {
		textViewer.setFocus();
	}

	@Persist
	public void save() {
		dirty.setDirty(false);
	}
	
	@PreDestroy
	public void dispose() {
		
		websocket.close();
	}
}