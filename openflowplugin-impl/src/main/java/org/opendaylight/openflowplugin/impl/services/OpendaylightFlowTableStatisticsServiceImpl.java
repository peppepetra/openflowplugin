/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import java.math.BigInteger;
import com.google.common.util.concurrent.JdkFutureAdapters;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table._case.MultipartRequestTableBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 */
public class OpendaylightFlowTableStatisticsServiceImpl extends CommonService implements
        OpendaylightFlowTableStatisticsService {

    @Override
    public Future<RpcResult<GetFlowTablesStatisticsOutput>> getFlowTablesStatistics(
            final GetFlowTablesStatisticsInput input) {
        return this.<GetFlowTablesStatisticsOutput, Void> handleServiceCall(PRIMARY_CONNECTION,
                new Function<BigInteger, Future<RpcResult<Void>>>() {

                    @Override
                    public Future<RpcResult<Void>> apply(final BigInteger IDConnection) {
                        final Xid xid = deviceContext.getNextXid();

                        // Create multipart request body for fetch all the group stats
                        final MultipartRequestTableCaseBuilder multipartRequestTableCaseBuilder = new MultipartRequestTableCaseBuilder();
                        final MultipartRequestTableBuilder multipartRequestTableBuilder = new MultipartRequestTableBuilder();
                        multipartRequestTableBuilder.setEmpty(true);
                        multipartRequestTableCaseBuilder.setMultipartRequestTable(multipartRequestTableBuilder.build());

                        // Set request body to main multipart request
                        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                                MultipartType.OFPMPFLOW, xid.getValue(), version);

                        mprInput.setMultipartRequestBody(multipartRequestTableCaseBuilder.build());
                        final Future<RpcResult<Void>> resultFromOFLib = deviceContext.getPrimaryConnectionContext()
                                .getConnectionAdapter().multipartRequest(mprInput.build());

                        return JdkFutureAdapters.listenInPoolThread(resultFromOFLib);
                    }
                });
    }

}