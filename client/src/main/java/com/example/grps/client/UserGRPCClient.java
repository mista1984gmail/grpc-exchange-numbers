package com.example.grps.client;


import com.example.grpc.dto.UserProto;
import com.example.grpc.rpc.UserRPCProto;
import com.example.grpc.rpc.UserRPCServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserGRPCClient {
    private static final Logger logger = LoggerFactory.getLogger(UserGRPCClient.class.getName());

    private final ManagedChannel channel;
    private final UserRPCServiceGrpc.UserRPCServiceBlockingStub blockingStub;
    List<UserProto.User>list=new ArrayList<>();

    public static void main(String[] args) throws Exception {
        UserGRPCClient client = new UserGRPCClient("localhost", 3333);
        try {
            client.request();
        } finally {
            client.shutdown();
        }
    }

    public UserGRPCClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());
    }

    public UserGRPCClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = UserRPCServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void request() {

        UserRPCProto.Request request = UserRPCProto.Request.newBuilder().build();
        Iterator<UserRPCProto.Response> response = blockingStub.listUsers(request);
        while (response.hasNext()) {
            var user = response.next().getUser(0);
            LocalDate expiryDate = parseDate(user.getBankCard().getCardExpiryDate());
            LocalDate dateLessThanThreeMonths = LocalDate.now().plusDays(90);
            if (expiryDate.isBefore(dateLessThanThreeMonths)) {
                logger.info("send email to: {}", user.getEmail());
                logger.info("Dear, {} ! Your card is expiring: {}. Contact your bank to extend the expiration date.",
                        user.getName(),
                        user.getBankCard().getCardExpiryDate());
            }
            list.add(user);
        }
        list.stream()
                .forEach(System.out::println);

        channel.shutdownNow();

    }
    public LocalDate parseDate(String date){
        return LocalDate.of(2000 + parseStringToIntForDate(date.substring(3,5)),
                        parseStringToIntForDate(date.substring(0,2)), 1)
                        .plusMonths(1);
    }

    public int parseStringToIntForDate(String str){
        return str.startsWith("0") ? Integer.parseInt(str.substring(1)) : Integer.parseInt(str);
    }

}
