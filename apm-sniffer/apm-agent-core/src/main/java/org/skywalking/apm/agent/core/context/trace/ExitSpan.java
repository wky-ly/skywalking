/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.agent.core.context.trace;

import org.skywalking.apm.agent.core.dictionary.DictionaryUtil;
import org.skywalking.apm.network.proto.SpanObject;
import org.skywalking.apm.network.trace.component.Component;

/**
 * 出口 Span
 *
 * The <code>ExitSpan</code> represents a service consumer point, such as Feign, Okhttp client for a Http service.
 *
 * It is an exit point or a leaf span(our old name) of trace tree.
 * In a single rpc call, because of a combination of discovery libs, there maybe contain multi-layer exit point:
 *
 * The <code>ExitSpan</code> only presents the first one.
 *
 * Such as: Dubbox -> Apache Httpcomponent -> ....(Remote)
 * The <code>ExitSpan</code> represents the Dubbox span, and ignore the httpcomponent span's info.
 *
 * @author wusheng
 */
public class ExitSpan extends StackBasedTracingSpan implements WithPeerInfo {

    /**
     * 节点地址
     */
    private String peer;
    /**
     * 节点编号
     */
    private int peerId;

    public ExitSpan(int spanId, int parentSpanId, String operationName, String peer) {
        super(spanId, parentSpanId, operationName);
        this.peer = peer;
        this.peerId = DictionaryUtil.nullValue();
    }

    public ExitSpan(int spanId, int parentSpanId, int operationId, int peerId) {
        super(spanId, parentSpanId, operationId);
        this.peer = null;
        this.peerId = peerId;
    }

    public ExitSpan(int spanId, int parentSpanId, int operationId, String peer) {
        super(spanId, parentSpanId, operationId);
        this.peer = peer;
        this.peerId = DictionaryUtil.nullValue();
    }

    public ExitSpan(int spanId, int parentSpanId, String operationName, int peerId) {
        super(spanId, parentSpanId, operationName);
        this.peer = null;
        this.peerId = peerId;
    }

    /**
     * Set the {@link #startTime}, when the first start, which means the first service provided.
     */
    @Override
    public ExitSpan start() {
        if (++stackDepth == 1) {
            super.start();
        }
        return this;
    }

    @Override
    public ExitSpan tag(String key, String value) {
        if (stackDepth == 1) {
            super.tag(key, value);
        }
        return this;
    }

    @Override
    public AbstractTracingSpan setLayer(SpanLayer layer) {
        if (stackDepth == 1) {
            return super.setLayer(layer);
        } else {
            return this;
        }
    }

    @Override
    public AbstractTracingSpan setComponent(Component component) {
        if (stackDepth == 1) {
            return super.setComponent(component);
        } else {
            return this;
        }
    }

    @Override
    public AbstractTracingSpan setComponent(String componentName) {
        if (stackDepth == 1) {
            return super.setComponent(componentName);
        } else {
            return this;
        }
    }

    @Override
    public ExitSpan log(Throwable t) {
        if (stackDepth == 1) {
            super.log(t);
        }
        return this;
    }

    @Override public SpanObject.Builder transform() {
        SpanObject.Builder spanBuilder = super.transform();
        if (peerId != DictionaryUtil.nullValue()) {
            spanBuilder.setPeerId(peerId);
        } else {
            if (peer != null) {
                spanBuilder.setPeer(peer);
            }
        }
        return spanBuilder;
    }

    @Override
    public AbstractTracingSpan setOperationName(String operationName) {
        if (stackDepth == 1) {
            return super.setOperationName(operationName);
        } else {
            return this;
        }
    }

    @Override
    public AbstractTracingSpan setOperationId(int operationId) {
        if (stackDepth == 1) {
            return super.setOperationId(operationId);
        } else {
            return this;
        }
    }

    @Override
    public int getPeerId() {
        return peerId;
    }

    @Override
    public String getPeer() {
        return peer;
    }

    @Override public boolean isEntry() {
        return false;
    }

    @Override public boolean isExit() {
        return true;
    }
}
