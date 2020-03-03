/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.controller.client;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.security.auth.callback.CallbackHandler;

import org.jboss.as.controller.client.impl.ClientConfigurationImpl;

/**
 * The configuration used to create the {@code ModelControllerClient}.
 *
 * @author Emanuel Muckenhuber
 */
public interface ModelControllerClientConfiguration extends Closeable {

    /**
     * Get the address of the remote host.
     *
     * @return the host name
     */
    String getHost();

    /**
     * Get the port of the remote host.
     *
     * @return the port number
     */
    int getPort();

    /**
     * Returns the requested protocol. If this is null the remoting protocol will be used. If this is http or https then
     * HTTP upgrade will be used.
     */
    String getProtocol();

    /**
     * Get the connection timeout when trying to connect to the server.
     *
     * @return the connection timeout
     */
    int getConnectionTimeout();

    /**
     * Get the security callback handler.
     *
     * @return the callback handler
     */
    CallbackHandler getCallbackHandler();

    /**
     * Get the executor service used for the controller client.
     *
     * @return the executor service
     */
    ExecutorService getExecutor();

    /**
     * Get the bind address used for the controller client.
     *
     * @return the bind address
     */
    String getClientBindAddress();

    class Builder {

        private static final AtomicInteger executorCount = new AtomicInteger();
        private static final ThreadGroup defaultThreadGroup = new ThreadGroup("management-client-thread");

        private String hostName;
        private String clientBindAddress;
        private int port;
        private CallbackHandler handler;
        private String protocol;
        private int connectionTimeout = 0;

        public Builder() {
        }

        /**
         * Sets the remote host name to which the client should connect.
         *
         * @param hostName the host name. Cannot be {@code null}
         * @return a builder to allow continued configuration
         */
        public Builder setHostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        /**
         * Sets the local address to which the client socket should be bound.
         *
         * @param clientBindAddress the local address, or {@code null} to choose one automatically
         * @return a builder to allow continued configuration
         */
        public Builder setClientBindAddress(String clientBindAddress) {
            this.clientBindAddress = clientBindAddress;
            return this;
        }

        /**
         * Sets the remote port to which the client should connect
         *
         * @param port the port
         * @return a builder to allow continued configuration
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the handler for callbacks to obtain authentication information.
         *
         * @param handler the handler, or {@code null} if callbacks are not supported.
         * @return a builder to allow continued configuration
         */
        public Builder setHandler(CallbackHandler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Sets the protocol to use for communicating with the remote process.
         *
         * @param protocol the protocol, or {@code null} if a default protocol for the {@link #setPort(int) specified
         *                 port} should be used
         * @return a builder to allow continued configuration
         */
        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Maximum time, in milliseconds, to wait for the connection to be established
         *
         * @param connectionTimeout the timeout
         * @return a builder to allow continued configuration
         */
        public Builder setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Builds the configuration object based on this builder's settings.
         *
         * @return the configuration
         */
        public ModelControllerClientConfiguration build() {
            ExecutorService executorService = Executors.newFixedThreadPool(6);
            return new ClientConfigurationImpl(hostName, port, handler, executorService, true,
                    connectionTimeout, protocol, clientBindAddress);
        }
    }
}
