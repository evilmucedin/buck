/*
 * Copyright (c) Facebook, Inc. and its affiliates.
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
 */

package com.facebook.buck.remoteexecution.grpc;

import build.bazel.remote.execution.v2.ContentAddressableStorageGrpc;
import build.bazel.remote.execution.v2.ContentAddressableStorageGrpc.ContentAddressableStorageFutureStub;
import build.bazel.remote.execution.v2.Digest;
import build.bazel.remote.execution.v2.ExecutionGrpc;
import build.bazel.remote.execution.v2.ExecutionGrpc.ExecutionStub;
import com.facebook.buck.core.exceptions.BuckUncheckedExecutionException;
import com.facebook.buck.core.util.immutables.BuckStyleValue;
import com.facebook.buck.core.util.log.Logger;
import com.facebook.buck.event.BuckEventBus;
import com.facebook.buck.remoteexecution.ContentAddressedStorageClient;
import com.facebook.buck.remoteexecution.RemoteExecutionClients;
import com.facebook.buck.remoteexecution.RemoteExecutionServiceClient;
import com.facebook.buck.remoteexecution.config.RemoteExecutionStrategyConfig;
import com.facebook.buck.remoteexecution.interfaces.MetadataProvider;
import com.facebook.buck.remoteexecution.interfaces.Protocol;
import com.facebook.buck.util.function.ThrowingConsumer;
import com.facebook.buck.util.types.Unit;
import com.google.bytestream.ByteStreamGrpc;
import com.google.bytestream.ByteStreamGrpc.ByteStreamStub;
import com.google.bytestream.ByteStreamProto.ReadRequest;
import com.google.bytestream.ByteStreamProto.ReadResponse;
import com.google.common.hash.HashCode;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** A RemoteExecution that sends jobs to a grpc-based remote execution service. */
public class GrpcRemoteExecutionClients implements RemoteExecutionClients {
  private static final Logger LOG = Logger.get(GrpcRemoteExecutionClients.class);
  public static final Protocol PROTOCOL = new GrpcProtocol();
  private final ContentAddressedStorageClient storage;
  private final ManagedChannel[] executionEngineChannels;
  private AtomicLong executionEngineChannelCtr;
  private final ManagedChannel casChannel;
  private final MetadataProvider metadataProvider;
  private final String instanceName;
  private final int casDeadline;
  private final ByteStreamStub byteStreamStub;

  /** A parsed read resource path. */
  @BuckStyleValue
  interface ParsedReadResource {
    String getInstanceName();

    Digest getDigest();
  }

  public GrpcRemoteExecutionClients(
      String instanceName,
      ManagedChannel executionEngineChannel,
      ManagedChannel casChannel,
      int casDeadline,
      MetadataProvider metadataProvider,
      BuckEventBus buckEventBus,
      RemoteExecutionStrategyConfig strategyConfig) {
    this(
        instanceName,
        new ManagedChannel[] {executionEngineChannel},
        casChannel,
        casDeadline,
        metadataProvider,
        buckEventBus,
        strategyConfig);
  }

  private static ManagedChannel[] createExecutionChannels(
      NettyChannelBuilder executionEngineChannel, int num_engine_channels) {
    ManagedChannel[] channels = new ManagedChannel[num_engine_channels];
    for (int i = 0; i < num_engine_channels; ++i) {
      channels[i] = executionEngineChannel.build();
    }
    return channels;
  }

  /** Creates multiple channels to engine for load balancing */
  public GrpcRemoteExecutionClients(
      String instanceName,
      NettyChannelBuilder executionEngineChannel,
      int numEngineConnections,
      ManagedChannel casChannel,
      int casDeadline,
      MetadataProvider metadataProvider,
      BuckEventBus buckEventBus,
      RemoteExecutionStrategyConfig strategyConfig) {
    this(
        instanceName,
        createExecutionChannels(executionEngineChannel, numEngineConnections),
        casChannel,
        casDeadline,
        metadataProvider,
        buckEventBus,
        strategyConfig);
  }

  private GrpcRemoteExecutionClients(
      String instanceName,
      ManagedChannel[] executionEngineChannels,
      ManagedChannel casChannel,
      int casDeadline,
      MetadataProvider metadataProvider,
      BuckEventBus buckEventBus,
      RemoteExecutionStrategyConfig strategyConfig) {
    this.executionEngineChannels = executionEngineChannels;
    this.executionEngineChannelCtr = new AtomicLong(0);
    this.casChannel = casChannel;
    this.metadataProvider = metadataProvider;
    this.instanceName = instanceName;
    this.casDeadline = casDeadline;

    this.byteStreamStub = ByteStreamGrpc.newStub(casChannel);
    this.storage =
        createStorage(
            ContentAddressableStorageGrpc.newFutureStub(casChannel),
            this.byteStreamStub,
            casDeadline,
            instanceName,
            PROTOCOL,
            buckEventBus,
            strategyConfig);
  }

  public static String getResourceName(String instanceName, Protocol.Digest digest) {
    return String.format("%s/blobs/%s/%d", instanceName, digest.getHash(), digest.getSize());
  }

  /** Reads a ByteStream onto the arg consumer. */
  public static ListenableFuture<Unit> readByteStream(
      String instanceName,
      Protocol.Digest digest,
      ByteStreamStub byteStreamStub,
      ThrowingConsumer<ByteString, IOException> dataConsumer,
      int casDeadline) {
    String name = getResourceName(instanceName, digest);
    SettableFuture<Unit> future = SettableFuture.create();
    byteStreamStub
        .withDeadlineAfter(casDeadline, TimeUnit.SECONDS)
        .read(
            ReadRequest.newBuilder().setResourceName(name).setReadLimit(0).setReadOffset(0).build(),
            new StreamObserver<ReadResponse>() {
              int size = 0;
              MessageDigest messageDigest = PROTOCOL.getMessageDigest();

              @Override
              public void onNext(ReadResponse value) {
                try {
                  ByteString data = value.getData();
                  size += data.size();
                  messageDigest.update(data.asReadOnlyByteBuffer());
                  dataConsumer.accept(data);
                } catch (IOException e) {
                  onError(e);
                }
              }

              @Override
              public void onError(Throwable t) {
                future.setException(t);
              }

              @Override
              public void onCompleted() {
                String digestHash = HashCode.fromBytes(messageDigest.digest()).toString();
                if (size == digest.getSize() && digestHash.equals(digest.getHash())) {
                  future.set(null);
                } else {
                  future.setException(
                      new BuckUncheckedExecutionException(
                          "Digest of received bytes: "
                              + digestHash
                              + ":"
                              + size
                              + " doesn't match expected digest: "
                              + digest));
                }
              }
            });
    return future;
  }

  @Override
  public RemoteExecutionServiceClient getRemoteExecutionService() {
    long len = this.executionEngineChannels.length;
    long ctr = this.executionEngineChannelCtr.getAndIncrement() % len;

    ExecutionStub executionStub = ExecutionGrpc.newStub(this.executionEngineChannels[(int) ctr]);
    return new GrpcRemoteExecutionServiceClient(
        executionStub, this.byteStreamStub, this.instanceName, getProtocol(), this.casDeadline);
  }

  @Override
  public ContentAddressedStorageClient getContentAddressedStorage() {
    return storage;
  }

  @Override
  public Protocol getProtocol() {
    return PROTOCOL;
  }

  @Override
  public void close() throws IOException {
    for (ManagedChannel c : executionEngineChannels) {
      closeChannel(c);
    }
    closeChannel(casChannel);
  }

  private static void closeChannel(ManagedChannel channel) {
    try {
      channel.shutdownNow().awaitTermination(3, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // This is hacky, but it ensures that we close the channels successfully on SIGINT.
      // Current SIGINT handler tries to cancel futures, which interrupts this flow and
      // channels are left open.
      try {
        LOG.debug(
            "Unable to close channel %s gracefully, trying to close again", channel.authority());
        channel.shutdownNow().awaitTermination(3, TimeUnit.SECONDS);
      } catch (InterruptedException ee) {
        throw new RuntimeException(e);
      }
      LOG.debug("Successfully closed channel %s", channel.authority());
      Thread.currentThread().interrupt();
    }
    LOG.debug("Successfully closed channel %s", channel.authority());
  }

  private ContentAddressedStorageClient createStorage(
      ContentAddressableStorageFutureStub storageStub,
      ByteStreamStub byteStreamStub,
      int casDeadline,
      String instanceName,
      Protocol protocol,
      BuckEventBus buckEventBus,
      RemoteExecutionStrategyConfig strategyConfig) {
    return new GrpcContentAddressableStorageClient(
        storageStub,
        byteStreamStub,
        casDeadline,
        instanceName,
        protocol,
        buckEventBus,
        metadataProvider.get(),
        strategyConfig.getOutputMaterializationThreads());
  }
}
