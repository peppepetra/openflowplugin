/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.impl.util.GroupUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.features.GroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Chaining;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.ChainingChecks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupAll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupFf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupIndirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupSelect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectLiveness;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectWeight;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.MeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBurst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterKbps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterPktps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;

/**
 * <p>
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.common
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *         </p>
 *         Created: Mar 31, 2015
 */
public class NodeStaticReplyTranslatorUtil {

    private NodeStaticReplyTranslatorUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Method transforms OFjava multipart reply model {@link MultipartReplyDesc} object
     * to inventory data model {@link FlowCapableNode} object.
     *
     * @param reply
     * @return
     */
    public static FlowCapableNode nodeDescTranslator(@CheckForNull final MultipartReplyDesc reply, final IpAddress ipAddress) {
        Preconditions.checkArgument(reply != null);
        final FlowCapableNodeBuilder flowCapAugBuilder = new FlowCapableNodeBuilder();
        flowCapAugBuilder.setDescription(reply.getDpDesc());
        flowCapAugBuilder.setHardware(reply.getHwDesc());
        flowCapAugBuilder.setManufacturer(reply.getMfrDesc());
        flowCapAugBuilder.setSoftware(reply.getSwDesc());
        flowCapAugBuilder.setSerialNumber(reply.getSerialNum());
        flowCapAugBuilder.setTable(Collections.<Table>emptyList());
        flowCapAugBuilder.setMeter(Collections.<Meter>emptyList());
        flowCapAugBuilder.setGroup(Collections.<Group>emptyList());
        if (ipAddress != null) {
            flowCapAugBuilder.setIpAddress(ipAddress);
        }
        return flowCapAugBuilder.build();
    }

    /**
     * Method transforms OFjava multipart reply model {@link MultipartReplyMeterFeatures} object
     * to inventory data model {@link NodeMeterFeatures} object.
     *
     * @param reply
     * @return
     */
    public static NodeMeterFeatures nodeMeterFeatureTranslator(@CheckForNull final MultipartReplyMeterFeatures reply) {
        Preconditions.checkArgument(reply != null);
        final MeterFeaturesBuilder meterFeature = new MeterFeaturesBuilder();
        meterFeature.setMaxBands(reply.getMaxBands());
        meterFeature.setMaxColor(reply.getMaxColor());
        meterFeature.setMaxMeter(new Counter32(reply.getMaxMeter()));
        final List<Class<? extends MeterBand>> meterBandTypes = new ArrayList<>();
        if (reply.getBandTypes().isOFPMBTDROP()) {
            meterBandTypes.add(MeterBandDrop.class);
        }
        if (reply.getBandTypes().isOFPMBTDSCPREMARK()) {
            meterBandTypes.add(MeterBandDscpRemark.class);
        }
        meterFeature.setMeterBandSupported(Collections.unmodifiableList(meterBandTypes));

        final List<java.lang.Class<? extends MeterCapability>> mCapability = new ArrayList<>();
        if (reply.getCapabilities().isOFPMFBURST()) {
            mCapability.add(MeterBurst.class);
        }
        if (reply.getCapabilities().isOFPMFKBPS()) {
            mCapability.add(MeterKbps.class);

        }
        if (reply.getCapabilities().isOFPMFPKTPS()) {
            mCapability.add(MeterPktps.class);

        }
        if (reply.getCapabilities().isOFPMFSTATS()) {
            mCapability.add(MeterStats.class);

        }
        meterFeature.setMeterCapabilitiesSupported(Collections.unmodifiableList(mCapability));
        return new NodeMeterFeaturesBuilder().setMeterFeatures(meterFeature.build()).build();
    }

    /**
     * Method transforms OFjava reply model {@link MultipartReplyGroupFeatures} object
     * to inventory data model {@link NodeGroupFeatures} object.
     *
     * @param reply
     * @return
     */
    public static NodeGroupFeatures nodeGroupFeatureTranslator(@CheckForNull final MultipartReplyGroupFeatures reply) {
        Preconditions.checkArgument(reply != null);
        final GroupFeaturesBuilder groupFeature = new GroupFeaturesBuilder();
        groupFeature.setMaxGroups(reply.getMaxGroups());

        final List<Class<? extends GroupType>> supportedGroups = new ArrayList<>();
        addSupportedGroups(reply, supportedGroups);
        groupFeature.setGroupTypesSupported(supportedGroups);

        final List<Class<? extends GroupCapability>> gCapability = new ArrayList<>();
        addGroupCapabilities(reply, gCapability);
        groupFeature.setGroupCapabilitiesSupported(gCapability);

        groupFeature.setActions(GroupUtil.extractGroupActionsSupportBitmap(reply.getActionsBitmap()));
        return new NodeGroupFeaturesBuilder().setGroupFeatures(groupFeature.build()).build();
    }

    private static void addGroupCapabilities(final MultipartReplyGroupFeatures reply, final List<Class<? extends GroupCapability>> gCapability) {
        if (reply.getCapabilities().isOFPGFCCHAINING()) {
            gCapability.add(Chaining.class);
        }
        if (reply.getCapabilities().isOFPGFCCHAININGCHECKS()) {
            gCapability.add(ChainingChecks.class);
        }
        if (reply.getCapabilities().isOFPGFCSELECTLIVENESS()) {
            gCapability.add(SelectLiveness.class);
        }
        if (reply.getCapabilities().isOFPGFCSELECTWEIGHT()) {
            gCapability.add(SelectWeight.class);
        }
    }

    private static void addSupportedGroups(final MultipartReplyGroupFeatures reply, final List<Class<? extends GroupType>> supportedGroups) {
        if (reply.getTypes().isOFPGTALL()) {
            supportedGroups.add(GroupAll.class);
        }
        if (reply.getTypes().isOFPGTSELECT()) {
            supportedGroups.add(GroupSelect.class);
        }
        if (reply.getTypes().isOFPGTINDIRECT()) {
            supportedGroups.add(GroupIndirect.class);
        }
        if (reply.getTypes().isOFPGTFF()) {
            supportedGroups.add(GroupFf.class);
        }
    }

    /**
     * Method transform {@link MultipartReplyTableFeatures} to list of {@link TableFeatures}. Every
     * table can have List of TableFeatures so add it directly to
     * {@link org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder}
     *
     * @param reply reply
     * @param version Openflow version
     * @param convertorExecutor convertor executor
     * @return list of table features
     */
    public static List<TableFeatures> nodeTableFeatureTranslator(@CheckForNull final MultipartReplyTableFeatures reply, final short version, @CheckForNull final ConvertorExecutor convertorExecutor) {
        Preconditions.checkArgument(reply != null);
        Preconditions.checkArgument(convertorExecutor != null);
        final Optional<List<TableFeatures>> tableFeaturesList = convertorExecutor.convert(reply, new VersionConvertorData(version));
        return tableFeaturesList.orElse(Collections.emptyList());
    }

    /**
     * Method build a ID Node Connector from version and port number.
     *
     * @param datapathId
     * @param portNo
     * @param version
     * @return
     */
    public static NodeConnectorId nodeConnectorId(@CheckForNull final String datapathId, final long portNo, final short version) {
        Preconditions.checkArgument(datapathId != null);
        final String logicalName = OpenflowPortsUtil.getPortLogicalName(version, portNo);
        return new NodeConnectorId(OFConstants.OF_URI_PREFIX + datapathId + ":" + (logicalName == null ? portNo : logicalName));
    }
}
