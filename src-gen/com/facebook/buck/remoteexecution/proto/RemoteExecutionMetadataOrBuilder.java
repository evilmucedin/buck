// @generated
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/com/facebook/buck/remoteexecution/proto/metadata.proto

package com.facebook.buck.remoteexecution.proto;

@javax.annotation.Generated(value="protoc", comments="annotations:RemoteExecutionMetadataOrBuilder.java.pb.meta")
public interface RemoteExecutionMetadataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:facebook.remote_execution.RemoteExecutionMetadata)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.facebook.remote_execution.RESessionID re_session_id = 1;</code>
   */
  boolean hasReSessionId();
  /**
   * <code>.facebook.remote_execution.RESessionID re_session_id = 1;</code>
   */
  com.facebook.buck.remoteexecution.proto.RESessionID getReSessionId();
  /**
   * <code>.facebook.remote_execution.RESessionID re_session_id = 1;</code>
   */
  com.facebook.buck.remoteexecution.proto.RESessionIDOrBuilder getReSessionIdOrBuilder();

  /**
   * <code>.facebook.remote_execution.BuckInfo buck_info = 2;</code>
   */
  boolean hasBuckInfo();
  /**
   * <code>.facebook.remote_execution.BuckInfo buck_info = 2;</code>
   */
  com.facebook.buck.remoteexecution.proto.BuckInfo getBuckInfo();
  /**
   * <code>.facebook.remote_execution.BuckInfo buck_info = 2;</code>
   */
  com.facebook.buck.remoteexecution.proto.BuckInfoOrBuilder getBuckInfoOrBuilder();

  /**
   * <code>.facebook.remote_execution.TraceInfo trace_info = 3;</code>
   */
  boolean hasTraceInfo();
  /**
   * <code>.facebook.remote_execution.TraceInfo trace_info = 3;</code>
   */
  com.facebook.buck.remoteexecution.proto.TraceInfo getTraceInfo();
  /**
   * <code>.facebook.remote_execution.TraceInfo trace_info = 3;</code>
   */
  com.facebook.buck.remoteexecution.proto.TraceInfoOrBuilder getTraceInfoOrBuilder();

  /**
   * <code>.facebook.remote_execution.CreatorInfo creator_info = 4;</code>
   */
  boolean hasCreatorInfo();
  /**
   * <code>.facebook.remote_execution.CreatorInfo creator_info = 4;</code>
   */
  com.facebook.buck.remoteexecution.proto.CreatorInfo getCreatorInfo();
  /**
   * <code>.facebook.remote_execution.CreatorInfo creator_info = 4;</code>
   */
  com.facebook.buck.remoteexecution.proto.CreatorInfoOrBuilder getCreatorInfoOrBuilder();

  /**
   * <code>.facebook.remote_execution.ExecutionEngineInfo engine_info = 5;</code>
   */
  boolean hasEngineInfo();
  /**
   * <code>.facebook.remote_execution.ExecutionEngineInfo engine_info = 5;</code>
   */
  com.facebook.buck.remoteexecution.proto.ExecutionEngineInfo getEngineInfo();
  /**
   * <code>.facebook.remote_execution.ExecutionEngineInfo engine_info = 5;</code>
   */
  com.facebook.buck.remoteexecution.proto.ExecutionEngineInfoOrBuilder getEngineInfoOrBuilder();

  /**
   * <code>.facebook.remote_execution.WorkerInfo worker_info = 6;</code>
   */
  boolean hasWorkerInfo();
  /**
   * <code>.facebook.remote_execution.WorkerInfo worker_info = 6;</code>
   */
  com.facebook.buck.remoteexecution.proto.WorkerInfo getWorkerInfo();
  /**
   * <code>.facebook.remote_execution.WorkerInfo worker_info = 6;</code>
   */
  com.facebook.buck.remoteexecution.proto.WorkerInfoOrBuilder getWorkerInfoOrBuilder();

  /**
   * <code>.facebook.remote_execution.CasClientInfo cas_client_info = 7;</code>
   */
  boolean hasCasClientInfo();
  /**
   * <code>.facebook.remote_execution.CasClientInfo cas_client_info = 7;</code>
   */
  com.facebook.buck.remoteexecution.proto.CasClientInfo getCasClientInfo();
  /**
   * <code>.facebook.remote_execution.CasClientInfo cas_client_info = 7;</code>
   */
  com.facebook.buck.remoteexecution.proto.CasClientInfoOrBuilder getCasClientInfoOrBuilder();
}
