package com.example.ingestionservice.config;

import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.AbstractStub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc;
import ru.tinkoff.piapi.contract.v1.UsersServiceGrpc;

@Configuration
public class GrpcConfig {

    @Value("${tinkoff.api.token}")
    private String token;

    @Bean
    public ManagedChannel investChannel() {
        ClientInterceptor authInterceptor = new ClientInterceptor() {
            @Override
            public <ReqT, RespT> io.grpc.ClientCall<ReqT, RespT> interceptCall(
                    io.grpc.MethodDescriptor<ReqT, RespT> method,
                    io.grpc.CallOptions callOptions,
                    io.grpc.Channel next) {
                return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
                    @Override
                    public void start(Listener<RespT> responseListener, Metadata headers) {
                        headers.put(
                                Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER),
                                "Bearer " + token
                        );
                        super.start(responseListener, headers);
                    }
                };
            }
        };

        return ManagedChannelBuilder
                .forAddress("invest-public-api.tinkoff.ru", 443)
                .useTransportSecurity()
                .intercept(authInterceptor)
                .build();
    }

    @Bean
    public UsersServiceGrpc.UsersServiceBlockingStub usersServiceStub(ManagedChannel channel) {
        return UsersServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public InstrumentsServiceGrpc.InstrumentsServiceBlockingStub instrumentsServiceStub(ManagedChannel channel) {
        return InstrumentsServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public MarketDataServiceGrpc.MarketDataServiceBlockingStub marketDataServiceStub(ManagedChannel channel) {
        return MarketDataServiceGrpc.newBlockingStub(channel);
    }
}
