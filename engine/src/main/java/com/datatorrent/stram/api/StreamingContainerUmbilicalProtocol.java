/*
 *  Copyright (c) 2012-2013 DataTorrent, Inc.
 *  All Rights Reserved.
 */
package com.datatorrent.stram.api;

import java.io.*;
import java.util.*;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.ipc.VersionedProtocol;

import com.datatorrent.api.AttributeMap;
import com.datatorrent.api.Context;
import com.datatorrent.api.Stats;
import com.datatorrent.stram.util.AbstractWritableAdapter;

/**
 * Protocol that streaming node child process uses to contact its parent
 * (application master) process
 * <p>
 * <br>
 * All communication between child and parent is via this protocol.
 *
 * <br>
 *
 * @since 0.3.2
 */
// @TokenInfo(JobTokenSelector.class)
@InterfaceAudience.Private
@InterfaceStability.Stable
public interface StreamingContainerUmbilicalProtocol extends VersionedProtocol {
  public static final long versionID = 201208081755L;

  /**
   * Initialization parameters for StramChild container. Container
   * wide settings remain effective as long as the process is running. Operators can
   * be deployed and removed dynamically.
   * <p>
   * <br>
   *
   */
  public static class StreamingContainerContext extends BaseContext implements ContainerContext
  {
    /**
     * Operators should start processing the initial window at this time.
     */
    public long startWindowMillis;

    public boolean deployBufferServer = true;

    /**
     * Constructor to enable deserialization using Hadoop's Writable interface.
     */
    private StreamingContainerContext()
    {
      super(null, null);
    }

    public StreamingContainerContext(AttributeMap map, Context parentContext)
    {
      super(map, parentContext);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
              .append("applicationAttributes", getAttributes()).toString();
    }

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    private static final long serialVersionUID = 201209071402L;
  }

  /**
   * Stats of deployed operator sent to the application master
   * <p>
   */
  public static class OperatorHeartbeat implements Serializable
  {
    private static final long serialVersionUID = 201208171625L;
    public ArrayList<ContainerStats.OperatorStats> windowStats = new ArrayList<ContainerStats.OperatorStats>();

    /**
     * The operator stats for the windows processed during the heartbeat interval.
     * @return ArrayList<OperatorStats>
     */
    public ArrayList<ContainerStats.OperatorStats> getOperatorStatsContainer() {
      return windowStats;
    }

    public static enum DeployState {
      ACTIVE,
      IDLE,// stopped processing (no more input etc.)
      FAILED // problemo!
    }

    /**
     * Operator id.
     */
    public int nodeId;

    public int getNodeId() {
      return nodeId;
    }

    public void setNodeId(int nodeId) {
      this.nodeId = nodeId;
    }

    /**
     * Time when the heartbeat was generated by the node.
     */
    public long generatedTms;

    public long getGeneratedTms() {
      return generatedTms;
    }

    public void setGeneratedTms(long generatedTms) {
      this.generatedTms = generatedTms;
    }

    /**
     * Number of milliseconds elapsed since last heartbeat. Other statistics
     * relative to this interval.
     */
    public long intervalMs;

    public long getIntervalMs() {
      return intervalMs;
    }

    public void setIntervalMs(long intervalMs) {
      this.intervalMs = intervalMs;
    }

    /**
     * State of the operator (processing, idle etc).
     */
    public String state;

    public String getState() {
      return state;
    }

    public void setState(String state) {
      this.state = state;
    }
  }

  public static class ContainerStats implements Stats
  {
    private static final long serialVersionUID = 201309131904L;
    public final String id;
    public ArrayList<OperatorHeartbeat> operators;

    public ContainerStats(String id)
    {
      this.id = id;
      operators = new ArrayList<OperatorHeartbeat>();
    }

    @Override
    public String toString()
    {
      return "ContainerStats{" + "id=" + id + ", operators=" + operators + '}';
    }

    public void addNodeStats(OperatorHeartbeat sn)
    {
      operators.add(sn);
    }
  }

  /**
   *
   * Sends stats aggregated by all operators in the this container to the stram
   * <p>
   * <br>
   *
   */
  public static class ContainerHeartbeat extends AbstractWritableAdapter
  {
    private static final long serialVersionUID = 1L;

    /**
     * Buffer server address for this container.
     * Port numbers are dynamically assigned and the master uses this info to deploy subscribers.
     */
    public String bufferServerHost;
    public int bufferServerPort;

    public String jvmName;
    // commented out because free memory is misleading because of GC. may want to revisit this.
    //public int memoryMBFree;
    public boolean restartRequested;

    public ContainerStats stats;

    public ContainerStats getContainerStats() {
      return stats;
    }

    public void setContainerStats(ContainerStats stats) {
      this.stats = stats;
    }

    public String getContainerId() {
      return stats.id;
    }

  }

  /**
   *
   * Request by stram as response to heartbeat for further communication
   * <p>
   * <br>
   * The child container will continue RPC communication depending on the type
   * of request.<br>
   * <br>
   *
   */
  public static class StramToNodeRequest implements Serializable {
    public static enum RequestType
    {
      START_RECORDING, STOP_RECORDING, SYNC_RECORDING, SET_PROPERTY
    }

    private static final long serialVersionUID = 1L;

    public int operatorId;
    public StramToNodeRequest.RequestType requestType;
    public long recoveryCheckpoint;
    public String portName;
    public boolean deleted;

    public String setPropertyKey;
    public String setPropertyValue;


    public boolean isDeleted()
    {
      return deleted;
    }

    public void setDeleted(boolean deleted)
    {
      this.deleted = deleted;
    }

    public int getOperatorId() {
      return operatorId;
    }

    public void setOperatorId(int id) {
      this.operatorId = id;
    }

    public StramToNodeRequest.RequestType getRequestType() {
      return requestType;
    }

    public void setRequestType(StramToNodeRequest.RequestType requestType) {
      this.requestType = requestType;
    }

    public long getRecoveryCheckpoint() {
      return recoveryCheckpoint;
    }

    public void setRecoveryCheckpoint(long recoveryCheckpoint) {
      this.recoveryCheckpoint = recoveryCheckpoint;
    }

    public String getPortName() {
      return portName;
    }

    public void setPortName(String portName) {
      this.portName = portName;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
              .append("operatorId", this.operatorId)
              .append("requestType", this.requestType)
              .append("portName", this.portName).toString();
    }
  }

  /**
   *
   * Response from the stram to the container heartbeat
   * <p>
   * <br>
   *
   */
  public static class ContainerHeartbeatResponse extends AbstractWritableAdapter {
    private static final long serialVersionUID = 1L;
    /**
     * Indicate container to exit heartbeat loop and shutdown.
     */
    public boolean shutdown;

    /**
     * Optional list of responses for operators in the container.
     */
    public List<StramToNodeRequest> nodeRequests;

    /**
     * Set when there are pending requests that wait for dependencies to
     * complete.
     */
    public boolean hasPendingRequests = false;

    /**
     * Set when operators need to be removed.
     */
    public List<Integer> undeployRequest;

    /**
     * Set when new operators need to be deployed.
     */
    public List<OperatorDeployInfo> deployRequest;

    /**
     * Set when dag purges a particular windowId as it's processed by all the operators.
     */
    public long committedWindowId = -1;
  }

  /**
   * The child container obtains its configuration context after launch.
   * <p>
   * <br>
   * Context will provide all information to initialize and prepare it for operator deployment<br>
   *
   * @param containerId
   * @throws IOException
   * <br>
   */
  StreamingContainerContext getInitContext(String containerId) throws IOException;

  void log(String containerId, String msg) throws IOException;

  /**
   * To be called periodically by child for heartbeat protocol.
   */
  ContainerHeartbeatResponse processHeartbeat(ContainerHeartbeat msg);

}
