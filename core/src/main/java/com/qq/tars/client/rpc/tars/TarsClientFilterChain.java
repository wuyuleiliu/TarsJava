package com.qq.tars.client.rpc.tars;

import com.qq.tars.client.rpc.ServantClient;
import com.qq.tars.common.AbstractFilterChain;
import com.qq.tars.common.Filter;
import com.qq.tars.common.FilterKind;
import com.qq.tars.common.util.BeanAccessor;
import com.qq.tars.net.client.Callback;
import com.qq.tars.net.core.Request;
import com.qq.tars.net.core.Response;
import com.qq.tars.rpc.protocol.ServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;

import java.util.List;
import java.util.concurrent.Future;

public class TarsClientFilterChain extends AbstractFilterChain<ServantClient> {

    private Request.InvokeStatus type;

    private Callback<TarsServantResponse> callback;

    private volatile Future<TarsServantResponse> future;

    public TarsClientFilterChain(List<Filter> filters, String servant,
                                 FilterKind kind, ServantClient target, Request.InvokeStatus type, Callback<TarsServantResponse> callback) {
        super(filters, servant, kind, target);
        this.type = type;
        this.callback = callback;
    }


    public Future<TarsServantResponse> getFuture() {
        return future;
    }

    @Override
    protected void doRealInvoke(Request request, Response response) throws Throwable {
        if (request instanceof TarsServantRequest && target != null) {
            TarsServantResponse tarsServantResponse = (TarsServantResponse) response;
            switch (type) {
                case SYNC_CALL:
                    try {
                        TarsServantResponse result = target.invokeWithSync((ServantRequest) request);
                        BeanAccessor.setBeanValue(tarsServantResponse, "cause", result.getCause());
                        BeanAccessor.setBeanValue(tarsServantResponse, "result", result.getResult());
                        BeanAccessor.setBeanValue(tarsServantResponse, "ret", result.getRet());
                    } catch (Exception e) {
                        BeanAccessor.setBeanValue(tarsServantResponse, "cause", e);
                        throw e;
                    }
                    return;
                case ASYNC_CALL:
                    target.invokeWithAsync((ServantRequest) request, callback);
                    return;
                case FUTURE_CALL:
                    target.invokeWithFuture((ServantRequest) request, callback);
                    return;

            }
        }
    }

}
