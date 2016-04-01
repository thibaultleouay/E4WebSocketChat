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

package fr.thibaultleouay.chat.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;

/**
 * 
 * This is a simple chat websocket server, that our rcp client connect to  
 *
 */

public class WebSocketServer extends AbstractVerticle {

	@Override
	public void start(Future<Void> fut) {
		vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>() {
			public void handle(final ServerWebSocket ws) {
				final String id = ws.textHandlerID();
				System.out.println("new connection from"+ ws.toString() + "id "+id);

				vertx.eventBus().consumer("chat",message -> {
					ws.writeFinalTextFrame((String) message.body());
				});

				ws.handler(new Handler<Buffer>() {
					public void handle(Buffer data) {
						// When our websocket receive data we publish it to our consumer 
						vertx.eventBus().publish("chat",data.toString());
					}
				});
				
				ws.closeHandler(handler ->{
					System.out.println("Close WS ");
				});


			}}
				).requestHandler(new Handler<HttpServerRequest>() {
					public void handle(HttpServerRequest req) {
						req.response().end("Chat");
						//Not usefull but it display chat on our browser 
					}
				}).listen(8080);
	}

}
